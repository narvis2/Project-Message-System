package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.Channel
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.ChannelEntity
import com.narvi.messagesystem.entity.UserChannelEntity
import com.narvi.messagesystem.repository.ChannelRepository
import com.narvi.messagesystem.repository.UserChannelRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val userChannelRepository: UserChannelRepository,
    private val sessionService: SessionService,
    private val userConnectionService: UserConnectionService,
) {

    fun getInviteCode(channelId: ChannelId): InviteCode? =
        channelRepository.findChannelInviteCodeByChannelId(channelId.id)?.let {
            InviteCode(it.inviteCode)
        } ?: run {
            log.warn("Invite code is not exist. channelId: {}", channelId)
            null
        }

    fun isJoined(channelId: ChannelId, userId: UserId): Boolean =
        userChannelRepository.existsByUserIdAndChannelId(userId.id, channelId.id)

    fun getParticipantIds(channelId: ChannelId): List<UserId> = userChannelRepository.findUserIdsByChannelId(
        channelId.id
    ).map { userId ->
        UserId(userId.userId)
    }

    fun getOnlineParticipantIds(channelId: ChannelId): List<UserId> =
        sessionService.getOnlineParticipantUserIds(channelId, getParticipantIds(channelId))

    @Transactional
    fun create(senderUserId: UserId, participantIds: List<UserId>, title: String): Pair<Channel?, ResultType> {
        if (title.isNullOrBlank()) {
            log.warn("Invalid args : title is empty.")
            return null to ResultType.INVALID_ARGS
        }

        // 여기서 + 1는 본인 (본인은 빠져있음)
        val headCount = participantIds.size + 1
        if (headCount > LIMIT_HEAD_COUNT) {
            log.warn(
                "Over limit channel. senderUserId: {}, participantIds count={}, title={}",
                senderUserId,
                participantIds.size,
                title
            )
            return null to ResultType.OVER_LIMIT
        }

        if (userConnectionService.countConnectionStatus(
                senderUserId, participantIds, UserConnectionStatus.ACCEPTED
            ) != participantIds.size.toLong()
        ) {
            log.warn("Included unconnected user. participantIds: {}", participantIds)
            return null to ResultType.NOT_ALLOWED
        }

        return try {
            val channelEntity = channelRepository.save(
                ChannelEntity(
                    title = title, headCount = headCount
                )
            )
            val channelId = channelEntity.channelId ?: throw IllegalStateException("Channel ID is null after save.")

            val userChannelEntities = participantIds.map { participantId ->
                UserChannelEntity(
                    userId = participantId.id, channelId = channelId, lastReadMsgSeq = 0
                )
            }.toMutableList().apply {
                add(UserChannelEntity(userId = senderUserId.id, channelId = channelId, lastReadMsgSeq = 0))
            }

            userChannelRepository.saveAll(userChannelEntities)
            Channel(ChannelId(id = channelId), title, headCount) to ResultType.SUCCESS
        } catch (ex: Exception) {
            log.error("Create failed. cause: {}", ex.message)
            throw ex
        }
    }

    fun enter(channelId: ChannelId, userId: UserId): Pair<String?, ResultType> {
        if (!isJoined(channelId, userId)) {
            log.warn("Enter channel failed. User not joined the channel. channelId: {}, userId: {}", channelId, userId)
            return null to ResultType.NOT_JOINED
        }

        val title = channelRepository.findChannelTitleByChannelId(channelId.id)?.title
        if (title.isNullOrBlank()) {
            log.warn("Enter channel failed. Channel not found. channelId: {}, userId: {}", channelId, userId)
            return null to ResultType.NOT_FOUND
        }

        return if (sessionService.setActiveChannel(userId, channelId)) {
            title to ResultType.SUCCESS
        } else {
            log.error("Enter channel failed. channelId: {}, userId: {}", channelId, userId)
            null to ResultType.FAILED
        }
    }

    companion object {
        private const val LIMIT_HEAD_COUNT = 100
        private val log = KotlinLogging.logger {}
    }
}