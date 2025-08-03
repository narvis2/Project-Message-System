package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.dto.websocket.inbound.WriteMessageRequest
import com.narvi.messagesystem.dto.websocket.outbound.MessageNotification
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class WriteMessageRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val messageRepository: MessageRepository,
) : BaseRequestHandler<WriteMessageRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: WriteMessageRequest) {
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