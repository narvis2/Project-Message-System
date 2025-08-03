package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode

data class FetchUserInviteCodeResponse(
    val inviteCode: InviteCode,
) : BaseMessage(MessageType.FETCH_USER_INVITECODE_RESPONSE)
