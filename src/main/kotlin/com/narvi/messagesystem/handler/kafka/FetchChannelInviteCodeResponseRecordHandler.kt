package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.FetchChannelInviteCodeResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.FetchChannelInviteCodeResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class FetchChannelInviteCodeResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<FetchChannelInviteCodeResponseRecord> {
    override fun handleRecord(record: FetchChannelInviteCodeResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = FetchChannelInviteCodeResponse(
                channelId = record.channelId,
                inviteCode = record.inviteCode
            ),
            record = record
        )
    }
}