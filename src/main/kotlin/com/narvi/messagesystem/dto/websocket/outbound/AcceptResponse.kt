package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data class AcceptResponse(
    val username: String,
) : BaseMessage(MessageType.ACCEPT_RESPONSE)
