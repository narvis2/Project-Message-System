package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.FetchChannelsResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.FetchChannelsResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class FetchChannelsResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<FetchChannelsResponseRecord> {
    override fun handleRecord(record: FetchChannelsResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = FetchChannelsResponse(record.channels),
            record = record
        )
    }
}