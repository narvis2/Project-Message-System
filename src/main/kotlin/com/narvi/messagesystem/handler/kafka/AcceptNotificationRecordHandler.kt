package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.AcceptNotificationRecord
import com.narvi.messagesystem.dto.websocket.outbound.AcceptNotification
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

/**
 * Message Server 가 Response 를 보내주면
 * Client 에게 WebSocket 으로 보내줌
 */
@Component
class AcceptNotificationRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<AcceptNotificationRecord> {
    override fun handleRecord(record: AcceptNotificationRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = AcceptNotification(username = record.username),
            record = record
        )
    }
}