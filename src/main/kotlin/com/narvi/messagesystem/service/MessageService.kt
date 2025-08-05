package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val channelService: ChannelService,
) {

    fun sendMessage(
        senderUserId: UserId,
        content: String,
        channelId: ChannelId,
        messageSender: Consumer<UserId>,
    ) {
        try {
            messageRepository.save(
                MessageEntity(
                    userId = senderUserId.id,
                    content = content
                )
            )
        } catch (ex: Exception) {
            log.error("Send message failed. cause: {}", ex.message)
            return
        }

        val participantIds = channelService.getParticipantIds(channelId)
        participantIds.filter { userId -> senderUserId != userId }
            .forEach { participantId ->
                // 유저가 온라인인 경우
                if (channelService.isOnline(participantId, channelId)) {
                    messageSender.accept(participantId)
                }
            }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}