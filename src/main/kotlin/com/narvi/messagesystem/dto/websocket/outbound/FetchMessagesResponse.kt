package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.Message

data class FetchMessagesResponse(
    val channelId: ChannelId,
    val messages: List<Message>,
) : BaseMessage(MessageType.FETCH_MESSAGES_RESPONSE)