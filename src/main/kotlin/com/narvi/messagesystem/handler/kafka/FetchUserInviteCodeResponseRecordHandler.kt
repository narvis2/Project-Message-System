package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.FetchUserInviteCodeResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.FetchUserInviteCodeResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class FetchUserInviteCodeResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<FetchUserInviteCodeResponseRecord> {
    override fun handleRecord(record: FetchUserInviteCodeResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = FetchUserInviteCodeResponse(record.inviteCode),
            record = record
        )
    }
}