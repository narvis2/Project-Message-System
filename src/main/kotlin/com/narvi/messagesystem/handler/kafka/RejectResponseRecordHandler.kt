package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.RejectResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.RejectResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class RejectResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<RejectResponseRecord> {
    override fun handleRecord(record: RejectResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = RejectResponse(
                username = record.username,
                status = record.status
            ),
            record = record
        )
    }
}