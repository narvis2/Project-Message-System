package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus

data class RejectResponse(
    val username: String,
    val status: UserConnectionStatus,
) : BaseMessage(MessageType.REJECT_RESPONSE)
