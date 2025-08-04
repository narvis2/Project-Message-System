package com.narvi.messagesystem.dto.projection

interface UserIdUsernameProjection {
    val userId: Long
    val username: String
    val inviterUserId: Long
}