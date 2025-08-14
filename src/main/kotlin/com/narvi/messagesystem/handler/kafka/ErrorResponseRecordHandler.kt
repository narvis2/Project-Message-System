package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.ErrorResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class ErrorResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<ErrorResponseRecord> {
    override fun handleRecord(record: ErrorResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = ErrorResponse(
                messageType = record.messageType,
                message = record.message
            ),
            record = record
        )
    }
}