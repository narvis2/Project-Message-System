package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data class AcceptNotification(
    val username: String,
) : BaseMessage(MessageType.NOTIFY_ACCEPT)
