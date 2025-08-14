package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.FetchConnectionsRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.FetchConnectionsRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchConnectionsRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<FetchConnectionsRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchConnectionsRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            FetchConnectionsRequestRecord(
                userId = senderUserId,
                status = request.status,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Fetch connections Failed."
                )
            )
        }
    }
}