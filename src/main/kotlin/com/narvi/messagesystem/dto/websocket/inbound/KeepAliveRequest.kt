package com.narvi.messagesystem.dto.websocket.inbound

import com.narvi.messagesystem.constant.MessageType

data object KeepAliveRequest : BaseRequest(type = MessageType.KEEP_ALIVE)