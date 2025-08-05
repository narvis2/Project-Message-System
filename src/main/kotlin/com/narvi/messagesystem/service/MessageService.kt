package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val channelService: ChannelService,
) {

    private val senderThreadPool: ExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    fun sendMessage(
        senderUserId: UserId,
        content: String,
        channelId: ChannelId,
        messageSender: Consumer<UserId>,
    ) {
        try {
            messageRepository.save(
                MessageEntity(
                    userId = senderUserId.id, content = content
                )
            )
        } catch (ex: Exception) {
            log.error("Send message failed. cause: {}", ex.message)
            return
        }

        // 온라인된 참여자
        channelService.getOnlineParticipantIds(channelId).filter { participantId -> senderUserId != participantId }
            .forEach { participantId ->
                CompletableFuture.runAsync({
                    messageSender.accept(participantId)
                }, senderThreadPool)
            }
    }

    companion object {
        private const val THREAD_POOL_SIZE = 10
        private val log = KotlinLogging.logger {}
    }
}