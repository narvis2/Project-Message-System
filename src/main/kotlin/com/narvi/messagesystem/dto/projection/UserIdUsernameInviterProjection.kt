package com.narvi.messagesystem.dto.projection

interface UserIdUsernameInviterProjection {
    val userId: Long
    val username: String
    val inviterUserId: Long
}