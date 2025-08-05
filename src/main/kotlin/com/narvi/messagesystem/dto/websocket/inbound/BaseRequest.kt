package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.narvi.messagesystem.constant.MessageType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = WriteMessage::class, name = MessageType.WRITE_MESSAGE),
    JsonSubTypes.Type(value = InviteRequest::class, name = MessageType.INVITE_REQUEST),
    JsonSubTypes.Type(value = FetchUserInviteCodeRequest::class, name = MessageType.FETCH_USER_INVITECODE_REQUEST),
    JsonSubTypes.Type(value = FetchConnectionsRequest::class, name = MessageType.FETCH_CONNECTIONS_REQUEST),
    JsonSubTypes.Type(value = RejectRequest::class, name = MessageType.REJECT_REQUEST),
    JsonSubTypes.Type(value = KeepAlive::class, name = MessageType.KEEP_ALIVE),
    JsonSubTypes.Type(value = AcceptRequest::class, name = MessageType.ACCEPT_REQUEST),
    JsonSubTypes.Type(value = DisconnectRequest::class, name = MessageType.DISCONNECT_REQUEST),
    JsonSubTypes.Type(value = CreateRequest::class, name = MessageType.CREATE_REQUEST),
    JsonSubTypes.Type(value = EnterRequest::class, name = MessageType.ENTER_REQUEST),
    JsonSubTypes.Type(value = FetchChannelInviteCodeRequest::class, name = MessageType.FETCH_CHANNEL_INVITECODE_REQUEST),
)
sealed class BaseRequest(
    val type: String
)