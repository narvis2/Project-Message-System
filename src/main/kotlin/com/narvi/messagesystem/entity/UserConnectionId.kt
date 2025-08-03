package com.narvi.messagesystem.entity

import java.io.Serializable

data class UserConnectionId(
    private val partnerAUserId: Long,
    private val partnerBUserId: Long,
) : Serializable {
}