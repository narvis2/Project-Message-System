package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.Channel
import com.narvi.messagesystem.dto.domain.Connection

class FetchChannelsResponse(
    val channels: List<Channel>
) : BaseMessage(MessageType.FETCH_CHANNELS_RESPONSE)
