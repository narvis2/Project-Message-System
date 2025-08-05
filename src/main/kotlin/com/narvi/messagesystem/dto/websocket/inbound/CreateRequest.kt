package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode

data class CreateRequest @JsonCreator constructor(
    @param:JsonProperty("title")
    val title: String,
    @param:JsonProperty("participantUsernames")
    val participantUsernames: List<String>,
) : BaseRequest(MessageType.CREATE_REQUEST)
