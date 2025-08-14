package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.JoinNotificationRecord
import com.narvi.messagesystem.dto.websocket.outbound.JoinNotification
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class JoinNotificationRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<JoinNotificationRecord> {
    override fun handleRecord(record: JoinNotificationRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = JoinNotification(
                channelId = record.channelId,
                title = record.title
            ),
            record = record
        )
    }
}