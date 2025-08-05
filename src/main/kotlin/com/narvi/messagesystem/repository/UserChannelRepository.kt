package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.UserIdProjection
import com.narvi.messagesystem.entity.UserChannelEntity
import com.narvi.messagesystem.entity.UserChannelId
import org.springframework.data.jpa.repository.JpaRepository

interface UserChannelRepository : JpaRepository<UserChannelEntity, UserChannelId> {

    fun existsByUserIdAndChannelId(UserId: Long, ChannelId: Long): Boolean

    fun findUserIdsByChannelId(channelId: Long): List<UserIdProjection>
}