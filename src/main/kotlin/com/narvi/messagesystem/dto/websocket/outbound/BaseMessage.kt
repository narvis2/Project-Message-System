package com.narvi.messagesystem.dto.websocket.outbound


sealed class BaseMessage(
    open val type: String,
) {
}