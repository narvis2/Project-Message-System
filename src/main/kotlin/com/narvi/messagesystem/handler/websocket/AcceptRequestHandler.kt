package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.AcceptRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.AcceptRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class AcceptRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<AcceptRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: AcceptRequest) {
        val acceptorUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(AcceptRequestRecord(acceptorUserId, request.username)) {
            clientNotificationService.sendErrorMessage(senderSession, ErrorResponse(request.type, "Accept failed"))
        }
    }
}