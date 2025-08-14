package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.EnterResponseRecord
import com.narvi.messagesystem.dto.websocket.outbound.EnterResponse
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class EnterResponseRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<EnterResponseRecord> {
    override fun handleRecord(record: EnterResponseRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = EnterResponse(
                channelId = record.channelId,
                title = record.title,
                lastReadMessageSeqId = record.lastReadMessageSeqId,
                lastChannelMessageSeqId = record.lastChannelMessageSeqId
            ),
            record = record
        )
    }
}