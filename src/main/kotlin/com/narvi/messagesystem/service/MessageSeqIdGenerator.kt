package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class MessageSeqIdGenerator(
    private val redisTemplate: StringRedisTemplate,
) {

    fun getNext(channelId: ChannelId): MessageSeqId? {
        val key = buildMessageSeqIdKey(channelId.id)
        return runCatching {
            redisTemplate.opsForValue().increment(key)?.let(::MessageSeqId)
        }.getOrElse {
            log.error("Redis increment failed. key: {}, cause: {}", key, it.message)
            null
        }
    }

    private fun buildMessageSeqIdKey(channelId: Long): String = "${KeyPrefix.CHANNEL}:${channelId}:seq_id"

    companion object {
        private val log = KotlinLogging.logger {}
    }
}