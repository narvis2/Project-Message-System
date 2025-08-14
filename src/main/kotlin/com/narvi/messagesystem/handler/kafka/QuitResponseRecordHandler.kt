package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.QuitResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.QuitResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class QuitResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<QuitResponseRecord> {
    override fun handleRecord(record: QuitResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = QuitResponse(record.channelId),
            record = record
        )
    }
}