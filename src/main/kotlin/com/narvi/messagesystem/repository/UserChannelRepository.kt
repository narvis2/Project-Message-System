package com.narvi.messagesystem.repository

import com.narvi.messagesystem.entity.UserChannelId
import com.narvi.messagesystem.entity.UserChannelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserChannelRepository : JpaRepository<UserChannelEntity, UserChannelId> {

    fun existsByUserIdAndChannelId(UserId: Long, ChannelId: Long): Boolean
}