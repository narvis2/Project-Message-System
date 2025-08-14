package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.InviteNotificationRecord
import com.narvi.messagesystem.dto.websocket.outbound.InviteNotification
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class InviteNotificationRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<InviteNotificationRecord> {
    override fun handleRecord(record: InviteNotificationRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = InviteNotification(record.username),
            record = record
        )
    }
}