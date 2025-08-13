package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.MessageSeqId

data class WriteMessageAck(
    val serial: Long,
    val messageSeqId: MessageSeqId,
) : BaseMessage(MessageType.WRITE_MESSAGE_ACK)