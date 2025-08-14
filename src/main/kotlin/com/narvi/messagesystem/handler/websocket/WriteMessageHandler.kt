package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.WriteMessageRecord
import com.narvi.messagesystem.dto.websocket.inbound.WriteMessage
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.kafka.KafkaProducer
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.MessageSeqIdGenerator
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class WriteMessageHandler(
    private val kafkaProducer: KafkaProducer,
    private val clientNotificationService: ClientNotificationService,
    private val messageSeqIdGenerator: MessageSeqIdGenerator,
) : BaseRequestHandler<WriteMessage> {

    override fun handleRequest(senderSession: WebSocketSession, request: WriteMessage) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId
        val errorCallback: () -> Unit = {
            clientNotificationService.sendErrorMessage(
                senderSession,
                ErrorResponse(request.type, "Write message Failed.")
            )
        }

        messageSeqIdGenerator.getNext(channelId)?.let { messageSeqId ->
            kafkaProducer.sendMessageUsingPartitionKey(
                channelId = channelId,
                userId = senderUserId,
                baseRecord = WriteMessageRecord(
                    userId = senderUserId,
                    serial = request.serial,
                    channelId = channelId,
                    content = request.content,
                    messageSeqId = messageSeqId
                ),
                errorCallback = errorCallback
            )
        } ?: run(errorCallback)
    }
}