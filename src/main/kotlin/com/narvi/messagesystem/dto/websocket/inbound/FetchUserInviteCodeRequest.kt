package com.narvi.messagesystem.dto.websocket.inbound

import com.narvi.messagesystem.constant.MessageType

data object FetchUserInviteCodeRequest : BaseRequest(MessageType.FETCH_USER_INVITECODE_REQUEST)
