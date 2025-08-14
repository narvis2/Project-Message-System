package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.AcceptResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.AcceptResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

/**
 * Message Server 가 Response 를 보내주면
 * Client 에게 WebSocket 으로 보내줌
 */
@Component
class AcceptResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<AcceptResponseRecord> {
    override fun handleRecord(record: AcceptResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = AcceptResponse(
                username = record.username
            ),
            record = record
        )
    }
}