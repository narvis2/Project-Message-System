package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.FetchMessagesResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.FetchMessagesResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class FetchMessagesResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<FetchMessagesResponseRecord> {
    override fun handleRecord(record: FetchMessagesResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = FetchMessagesResponse(
                channelId = record.channelId,
                messages = record.messages
            ),
            record = record
        )
    }
}