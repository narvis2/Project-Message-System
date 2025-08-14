package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId
import com.narvi.messagesystem.dto.domain.UserId

data class WriteMessageRecord @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: UserId,
    @param:JsonProperty("serial") val serial: Long,
    @param:JsonProperty("channelId") val channelId: ChannelId,
    @param:JsonProperty("content") val content: String,
    @param:JsonProperty("messageSeqId") val messageSeqId: MessageSeqId, // Connection 서버에서 SeqId 를 발급받고 Message Server 로 전송
) : BaseRecord(MessageType.WRITE_MESSAGE)