package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId

data class QuitResponse(val channelId: ChannelId) : BaseMessage(MessageType.QUIT_RESPONSE)
