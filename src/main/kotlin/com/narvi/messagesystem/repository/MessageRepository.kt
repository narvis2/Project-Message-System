package com.narvi.messagesystem.repository

import com.narvi.messagesystem.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<MessageEntity, Long> {
}