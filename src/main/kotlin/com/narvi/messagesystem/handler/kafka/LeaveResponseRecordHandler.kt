package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.LeaveResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.LeaveResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class LeaveResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<LeaveResponseRecord> {
    override fun handleRecord(record: LeaveResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = LeaveResponse,
            record = record
        )
    }
}