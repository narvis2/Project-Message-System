package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.ChannelTitleProjection
import com.narvi.messagesystem.dto.projection.InviteCodeProjection
import com.narvi.messagesystem.entity.ChannelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelRepository : JpaRepository<ChannelEntity, Long> {

    fun findChannelTitleByChannelId(channelId: Long): ChannelTitleProjection?

    fun findChannelInviteCodeByChannelId(channelId: Long): InviteCodeProjection?


}