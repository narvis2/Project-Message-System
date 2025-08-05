package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.dto.websocket.inbound.WriteMessage
import com.narvi.messagesystem.dto.websocket.outbound.MessageNotification
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class WriteMessageHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val messageRepository: MessageRepository,
) : BaseRequestHandler<WriteMessage> {

    override fun handleRequest(senderSession: WebSocketSession, request: WriteMessage) {
        val receivedMessage = MessageNotification(
            username = request.username,
            content = request.content
        )

        messageRepository.save(
            MessageEntity(
                userName = receivedMessage.username,
                content = receivedMessage.content
            )
        )

        webSocketSessionManager.getSessions().forEach { participantSession ->
            if (senderSession.id != participantSession.id) {
                webSocketSessionManager.sendMessage(participantSession, receivedMessage)
            }
        }
    }
}