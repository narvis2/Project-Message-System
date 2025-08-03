package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode

data class InviteResponse(
    val inviteCode: InviteCode,
    val status: UserConnectionStatus,
) : BaseMessage(MessageType.INVITE_RESPONSE)
