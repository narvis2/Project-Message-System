package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.FetchChannelInviteCodeRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.FetchChannelInviteCodeRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchChannelInviteCodeRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<FetchChannelInviteCodeRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: FetchChannelInviteCodeRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        kafkaProducer.sendRequest(
            FetchChannelInviteCodeRequestRecord(
                userId = senderUserId,
                channelId = request.channelId,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Fetch channel invitecode Failed."
                )
            )
        }
    }
}