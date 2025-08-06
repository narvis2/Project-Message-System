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
import jakarta.persistence.EntityNotFoundException
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

    fun getOnlineParticipantIds(channelId: ChannelId, userIds: List<UserId>): List<UserId> =
        sessionService.getOnlineParticipantUserIds(channelId, userIds)

    fun getChannel(inviteCode: InviteCode): Channel? = channelRepository.findChannelByInviteCode(inviteCode.code)?.let {
        Channel(
            channelId = ChannelId(id = it.channelId), title = it.title, headCount = it.headCount
        )
    }

    fun getChannels(userId: UserId): List<Channel> = userChannelRepository.findChannelsByUserId(userId.id).map {
        Channel(channelId = ChannelId(id = it.channelId), title = it.title, headCount = it.headCount)
    }

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

    @Transactional
    fun join(inviteCode: InviteCode, userId: UserId): Pair<Channel?, ResultType> {
        val channel = getChannel(inviteCode) ?: return null to ResultType.NOT_FOUND
        if (isJoined(channel.channelId, userId)) {
            return null to ResultType.ALREADY_JOINED
        }

        if (channel.headCount >= LIMIT_HEAD_COUNT) {
            return null to ResultType.OVER_LIMIT
        }

        val channelEntity = channelRepository.findForUpdateByChannelId(channel.channelId.id)
            ?: throw EntityNotFoundException("Invalid channelId: ${channel.channelId}")

        // Join 가능
        if (channelEntity.headCount < LIMIT_HEAD_COUNT) {
            channelEntity.headCount += 1
            userChannelRepository.save(
                UserChannelEntity(
                    userId = userId.id, channelId = channel.channelId.id, lastReadMsgSeq = 0
                )
            )
        }

        return channel to ResultType.SUCCESS
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

    fun leave(userId: UserId): Boolean = sessionService.removeActiveChannel(userId)

    @Transactional
    fun quit(channelId: ChannelId, userId: UserId): ResultType {
        if (!isJoined(channelId, userId)) {
            return ResultType.NOT_JOINED
        }

        val channelEntity = channelRepository.findForUpdateByChannelId(channelId.id)
            ?: throw EntityNotFoundException("Invalid channelId: $channelId")

        if (channelEntity.headCount > 0) {
            channelEntity.headCount -= 1
        } else {
            log.error("Count is already zero. channelId: {}, userId: {}", channelId, userId)
        }

        userChannelRepository.deleteByUserIdAndChannelId(userId.id, channelId.id)
        return ResultType.SUCCESS
    }

    companion object {
        private const val LIMIT_HEAD_COUNT = 100
        private val log = KotlinLogging.logger {}
    }
}