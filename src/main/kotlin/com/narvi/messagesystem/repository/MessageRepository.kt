package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.MessageInfoProjection
import com.narvi.messagesystem.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MessageRepository : JpaRepository<MessageEntity, Long> {

    @Query("SELECT MAX(m.messageSequence) FROM MessageEntity m WHERE m.channelId = :channelId")
    fun findLastMessageSequenceByChannelId(@Param("channelId") channelId: Long): Long?

    fun findByChannelIdAndMessageSequenceBetween(
        channelId: Long,
        startMessageSequence: Long,
        endMessageSequence: Long
    ): List<MessageInfoProjection>
}