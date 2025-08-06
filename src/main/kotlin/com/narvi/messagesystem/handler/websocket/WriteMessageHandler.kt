package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.WriteMessage
import com.narvi.messagesystem.dto.websocket.outbound.MessageNotification
import com.narvi.messagesystem.service.MessageService
import com.narvi.messagesystem.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class WriteMessageHandler(
    private val userService: UserService,
    private val messageService: MessageService,
) : BaseRequestHandler<WriteMessage> {

    override fun handleRequest(senderSession: WebSocketSession, request: WriteMessage) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId
        val content = request.content
        val senderUsername = userService.getUsername(senderUserId) ?: "unknown"
        messageService.sendMessage(
            senderUserId,
            content,
            channelId,
            MessageNotification(
                channelId = channelId,
                username = senderUsername,
                content = content
            )
        )
    }
}