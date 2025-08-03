package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data class ErrorResponse(
    val messageType: String,
    val message: String,
) : BaseMessage(MessageType.ERROR)