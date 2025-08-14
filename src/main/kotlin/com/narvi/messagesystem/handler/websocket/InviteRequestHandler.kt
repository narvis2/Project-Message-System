package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.DisconnectRequestRecord
import com.narvi.messagesystem.dto.kafka.InviteRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.InviteRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.InviteNotification
import com.narvi.messagesystem.dto.websocket.outbound.InviteResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class InviteRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val kafkaProducer: KafkaProducer,
) : BaseRequestHandler<InviteRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: InviteRequest) {
        val inviterUserId = senderSession.attributes[IdKey.USER_ID.value] as? UserId ?: return

        kafkaProducer.sendRequest(
            InviteRequestRecord(
                userId = inviterUserId,
                userInviteCode = request.userInviteCode
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Invite Failed."
                )
            )
        }
    }
}