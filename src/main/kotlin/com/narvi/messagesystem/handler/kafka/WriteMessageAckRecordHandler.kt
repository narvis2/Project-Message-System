package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.WriteMessageAckRecord
import com.narvi.messagesystem.dto.websocket.outbound.WriteMessageAck
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component

@Component
class WriteMessageAckRecordHandler(
    private val clientNotificationService: ClientNotificationService,
) : BaseRecordHandler<WriteMessageAckRecord> {
    override fun handleRecord(record: WriteMessageAckRecord) {
        clientNotificationService.sendMessage(
            userId = record.userId,
            message = WriteMessageAck(
                serial = record.serial,
                messageSeqId = record.messageSeqId
            ),
            record = record
        )
    }
}