package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.narvi.messagesystem.constant.MessageType

// TODO:: Sealed Class 로 변경 고민해보기
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageRequest::class, name = MessageType.MESSAGE),
    JsonSubTypes.Type(value = KeepAliveRequest::class, name = MessageType.KEEP_ALIVE)
)
sealed class BaseRequest(
    open val type: String
)