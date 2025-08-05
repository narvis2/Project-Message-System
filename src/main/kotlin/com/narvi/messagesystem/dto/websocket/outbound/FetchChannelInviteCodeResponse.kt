package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode

data class FetchChannelInviteCodeResponse(
    val channelId: ChannelId,
    val inviteCode: InviteCode,
) : BaseMessage(MessageType.FETCH_CHANNEL_INVITECODE_RESPONSE)
