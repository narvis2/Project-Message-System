package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.FetchMessagesRequestRecord
import com.narvi.messagesystem.dto.websocket.inbound.FetchMessagesRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchMessagesRequestHandler(
    private val kafkaProducer: KafkaProducer,
    private val clientNotificationService: ClientNotificationService,
) : BaseRequestHandler<FetchMessagesRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: FetchMessagesRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId

        kafkaProducer.sendMessageUsingPartitionKey(
            channelId = channelId,
            userId = senderUserId,
            baseRecord = FetchMessagesRequestRecord(
                userId = senderUserId,
                channelId = channelId,
                startMessageSeqId = request.startMessageSeqId,
                endMessageSeqId = request.endMessageSeqId,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Fetch messages Failed."
                )
            )
        }
    }
}