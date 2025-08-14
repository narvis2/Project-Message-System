package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.MessageNotificationRecord
import com.narvi.messagesystem.service.MessageService
import org.springframework.stereotype.Component

// 해당 채널에 참여한 모든 참여자에게 모두 보내줘야함
@Component
class MessageNotificationRecordHandler(
    private val messageService: MessageService,
) : BaseRecordHandler<MessageNotificationRecord> {
    override fun handleRecord(record: MessageNotificationRecord) {
        messageService.sendMessage(record)
    }
}