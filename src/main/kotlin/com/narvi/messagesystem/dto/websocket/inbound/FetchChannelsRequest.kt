package com.narvi.messagesystem.dto.websocket.inbound

import com.narvi.messagesystem.constant.MessageType

data object FetchChannelsRequest : BaseRequest(MessageType.FETCH_CHANNELS_REQUEST)
