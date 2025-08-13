package com.narvi.messagesystem.service

import com.narvi.messagesystem.database.ShardContext
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.projection.MessageInfoProjection
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * message db 로 가는 connection 은 항상 Shard 를 찾아야하고
 * 항상 새로 맺어야 함
 */
@Service
class MessageShardService(
    private val messageRepository: MessageRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun findLastMessageSequenceByChannelId(channelId: ChannelId): MessageSeqId =
        ShardContext.ShardContextScope(channelId.id).use {
            messageRepository.findLastMessageSequenceByChannelId(channelId.id)?.let(::MessageSeqId) ?: MessageSeqId(0L)
        }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun findByChannelIdAndMessageSequenceBetween(
        channelId: ChannelId,
        startMessageSeqId: MessageSeqId,
        endMessageSeqId: MessageSeqId,
    ): List<MessageInfoProjection> = ShardContext.ShardContextScope(channelId.id).use {
        messageRepository.findByChannelIdAndMessageSequenceBetween(
            channelId.id,
            startMessageSeqId.id,
            endMessageSeqId.id
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(channelId: ChannelId, messageSeqId: MessageSeqId, senderUserId: UserId, content: String) {
        ShardContext.ShardContextScope(channelId.id).use {
            messageRepository.save(
                MessageEntity(
                    channelId = channelId.id,
                    messageSequence = messageSeqId.id,
                    userId = senderUserId.id,
                    content = content
                )
            )
        }
    }
}