package com.narvi.messagesystem.dto.domain

import com.fasterxml.jackson.annotation.JsonCreator

data class Message @JsonCreator constructor(
    val channelId: ChannelId,
    val messageSeqId: MessageSeqId,
    val username: String,
    val content: String
)
