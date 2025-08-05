package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.Channel
import com.narvi.messagesystem.dto.domain.ChannelId
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
) {

    fun isJoined(channelId: ChannelId, userId: UserId): Boolean =
        userChannelRepository.existsByUserIdAndChannelId(userId.id, channelId.id)

    @Transactional
    fun create(senderUserId: UserId, participantId: UserId, title: String): Pair<Channel?, ResultType> {
        if (title.isNullOrBlank()) {
            log.warn("Invalid args : title is empty.")
            return null to ResultType.INVALID_ARGS
        }

        return try {
            val HEAD_COUNT = 2
            val channelEntity = channelRepository.save(
                ChannelEntity(
                    title = title,
                    headCount = HEAD_COUNT
                )
            )
            val channelId = channelEntity.channelId ?: throw IllegalStateException("Channel ID is null after save.")
            val userChannelEntities = listOf(
                UserChannelEntity(
                    userId = senderUserId.id,
                    channelId = channelId,
                    lastReadMsgSeq = 0
                ),
                UserChannelEntity(
                    userId = participantId.id,
                    channelId = channelId,
                    lastReadMsgSeq = 0
                )
            )
            userChannelRepository.saveAll(userChannelEntities)

            Channel(ChannelId(id = channelId), title, HEAD_COUNT) to ResultType.SUCCESS
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
        private val log = KotlinLogging.logger {}
    }
}