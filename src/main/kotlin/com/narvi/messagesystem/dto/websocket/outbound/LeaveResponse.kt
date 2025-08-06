package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data object LeaveResponse : BaseMessage(MessageType.LEAVE_RESPONSE)
