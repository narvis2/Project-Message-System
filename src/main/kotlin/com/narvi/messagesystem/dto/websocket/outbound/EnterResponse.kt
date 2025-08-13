package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId

data class EnterResponse(
    val channelId: ChannelId,
    val title: String,
    val lastReadMessageSeqId: MessageSeqId,
    val lastChannelMessageSeqId: MessageSeqId,
) : BaseMessage(MessageType.ENTER_RESPONSE)
