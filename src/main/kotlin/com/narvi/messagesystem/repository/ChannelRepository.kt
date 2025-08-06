package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.ChannelProjection
import com.narvi.messagesystem.dto.projection.ChannelTitleProjection
import com.narvi.messagesystem.dto.projection.InviteCodeProjection
import com.narvi.messagesystem.entity.ChannelEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ChannelRepository : JpaRepository<ChannelEntity, Long> {

    fun findChannelTitleByChannelId(channelId: Long): ChannelTitleProjection?

    fun findChannelInviteCodeByChannelId(channelId: Long): InviteCodeProjection?

    fun findChannelByInviteCode(inviteCode: String): ChannelProjection?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByChannelId(channelId: Long): ChannelEntity?
}