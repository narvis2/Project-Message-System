package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.CreateResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.CreateResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class CreateResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<CreateResponseRecord> {
    override fun handleRecord(record: CreateResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = CreateResponse(
                channelId = record.channelId,
                title = record.title
            ),
            record = record
        )
    }
}