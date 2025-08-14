package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.CreateRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.CreateRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class CreateRequestHandler(
    private val kafkaProducer: KafkaProducer,
    private val clientNotificationService: ClientNotificationService,
) : BaseRequestHandler<CreateRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: CreateRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            CreateRequestRecord(
                userId = senderUserId,
                title = request.title,
                participantUsernames = request.participantUsernames
            )
        ) {
            clientNotificationService.sendErrorMessage(senderSession, ErrorResponse(request.type, "Create Failed."))
        }
    }
}