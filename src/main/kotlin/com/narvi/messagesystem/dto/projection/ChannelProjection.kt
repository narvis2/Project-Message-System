package com.narvi.messagesystem.dto.projection

interface ChannelProjection {
    val channelId: Long
    val title: String
    val headCount: Int
}