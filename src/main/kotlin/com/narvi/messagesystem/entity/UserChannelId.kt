package com.narvi.messagesystem.entity

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserChannelId(
    val userId: Long,
    val channelId: Long,
) : Serializable