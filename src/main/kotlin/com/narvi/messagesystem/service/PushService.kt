package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.UserId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PushService {
    // FCM Push 가 필요한 EVENT 만 처리
    private val pushMessageTypes = HashSet<String>()

    fun registerPushMessageType(messageType: String) {
        pushMessageTypes.add(messageType)
    }

    fun pushMessage(userId: UserId?, messageType: String, message: String?) {
        if (pushMessageTypes.contains(messageType)) {
            log.info("push message: {} to user: {}", message, userId)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}