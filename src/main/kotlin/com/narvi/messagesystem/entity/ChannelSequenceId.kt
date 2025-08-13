package com.narvi.messagesystem.entity

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ChannelSequenceId(
    val channelId: Long,
    val messageSequence: Long,
) : Serializable