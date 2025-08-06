package com.narvi.messagesystem.dto.websocket.inbound

import com.narvi.messagesystem.constant.MessageType

data object LeaveRequest : BaseRequest(MessageType.LEAVE_REQUEST)
