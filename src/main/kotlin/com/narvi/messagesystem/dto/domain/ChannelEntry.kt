package com.narvi.messagesystem.dto.domain

data class ChannelEntry(
    val title: String,
    val lastReadMessageSeqId: MessageSeqId,
    val lastChannelMessageSeqId: MessageSeqId,
)
