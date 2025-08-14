package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.InviteResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.InviteResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class InviteResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<InviteResponseRecord> {
    override fun handleRecord(record: InviteResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = InviteResponse(
                inviteCode = record.inviteCode,
                status = record.status
            ),
            record = record
        )
    }
}