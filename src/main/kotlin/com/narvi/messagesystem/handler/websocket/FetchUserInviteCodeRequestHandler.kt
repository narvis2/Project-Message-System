package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.FetchUserInviteCodeRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.FetchUserInviteCodeRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchUserInviteCodeRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<FetchUserInviteCodeRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchUserInviteCodeRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            FetchUserInviteCodeRequestRecord(
                userId = senderUserId,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Fetch user invitecode Failed."
                )
            )
        }
    }
}