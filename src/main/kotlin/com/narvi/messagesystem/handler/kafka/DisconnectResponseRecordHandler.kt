package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.DisconnectResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.DisconnectResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class DisconnectResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<DisconnectResponseRecord> {
    override fun handleRecord(record: DisconnectResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = DisconnectResponse(
                username = record.username,
                status = record.status
            ),
            record = record
        )
    }
}