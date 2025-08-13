package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.ReadMessageAck
import com.narvi.messagesystem.service.MessageService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class ReadMessageAckHandler(
    private val messageService: MessageService,
) : BaseRequestHandler<ReadMessageAck> {

    override fun handleRequest(senderSession: WebSocketSession, request: ReadMessageAck) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        messageService.updateLastReadMsgSeq(
            senderUserId,
            request.channelId,
            request.messageSeqId
        )
    }
}