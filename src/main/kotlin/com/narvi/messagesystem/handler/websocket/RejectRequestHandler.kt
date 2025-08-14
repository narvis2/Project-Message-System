package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.DisconnectRequestRecord
import com.narvi.messagesystem.dto.kafka.RejectRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.RejectRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.RejectResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class RejectRequestHandler(
    private val kafkaProducer: KafkaProducer,
    private val clientNotificationService: ClientNotificationService,
) : BaseRequestHandler<RejectRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: RejectRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            RejectRequestRecord(
                userId = senderUserId,
                username = request.username,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Reject Failed."
                )
            )
        }
    }
}