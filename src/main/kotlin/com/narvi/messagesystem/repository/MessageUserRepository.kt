package com.narvi.messagesystem.repository

import com.narvi.messagesystem.entity.MessageUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MessageUserRepository : JpaRepository<MessageUserEntity, Long> {
    fun findByUsername(username: String): Optional<MessageUserEntity>
}