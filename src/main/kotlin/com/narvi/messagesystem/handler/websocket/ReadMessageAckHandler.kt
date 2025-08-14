package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.ReadMessageAckRecord
import com.narvi.messagesystem.dto.websocket.inbound.ReadMessageAck
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class ReadMessageAckHandler(
    private val kafkaProducer: KafkaProducer,
    private val clientNotificationService: ClientNotificationService
) : BaseRequestHandler<ReadMessageAck> {

    override fun handleRequest(senderSession: WebSocketSession, request: ReadMessageAck) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId

        kafkaProducer.sendMessageUsingPartitionKey(
            channelId = channelId,
            userId = senderUserId,
            baseRecord = ReadMessageAckRecord(
                userId = senderUserId,
                channelId = channelId,
                messageSeqId = request.messageSeqId,
            )
        ) {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(
                    request.type,
                    "Read message ack Failed."
                )
            )
        }
    }
}