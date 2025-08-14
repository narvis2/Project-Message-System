package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.FetchChannelsRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.FetchChannelsRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchChannelsRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<FetchChannelsRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchChannelsRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            FetchChannelsRequestRecord(
                userId = senderUserId,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Fetch channels Failed."
                )
            )
        }
    }
}