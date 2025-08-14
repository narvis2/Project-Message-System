package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.MessageSeqId
import com.narvi.messagesystem.dto.domain.UserId

data class WriteMessageAckRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("serial")
    val serial: Long,
    @param:JsonProperty("messageSeqId")
    val messageSeqId: MessageSeqId,
) : BaseRecord(MessageType.WRITE_MESSAGE_ACK)
