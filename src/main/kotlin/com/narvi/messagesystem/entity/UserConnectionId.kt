package com.narvi.messagesystem.entity

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserConnectionId(
    val partnerAUserId: Long,
    val partnerBUserId: Long,
) : Serializable