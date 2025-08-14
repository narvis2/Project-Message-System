package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.FetchConnectionsResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.FetchConnectionsResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class FetchConnectionsResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<FetchConnectionsResponseRecord> {
    override fun handleRecord(record: FetchConnectionsResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = FetchConnectionsResponse(record.connections),
            record = record
        )
    }
}