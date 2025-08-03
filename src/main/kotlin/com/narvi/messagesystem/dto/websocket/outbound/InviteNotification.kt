package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data class InviteNotification(
    val username: String,
) : BaseMessage(MessageType.ASK_INVITE)