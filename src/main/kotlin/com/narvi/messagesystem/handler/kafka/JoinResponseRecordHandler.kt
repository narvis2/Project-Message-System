package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.JoinResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.JoinResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class JoinResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<JoinResponseRecord> {
    override fun handleRecord(record: JoinResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            // TODO:: Git 에는 JoinNotification 을 넣어주고 있음
            message = JoinResponse(
                channelId = record.channelId,
                title = record.title
            ),
            record = record
        )
    }
}