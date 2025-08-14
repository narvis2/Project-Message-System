package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.kafka.ListenTopicCreator
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SessionService(
    private val httpSessionRepository: SessionRepository<out Session>,
    private val stringRedisTemplate: StringRedisTemplate,
    private val listenTopicCreator: ListenTopicCreator,
    private val cacheService: CacheService,
) {

    private val TTL: Long = 300

    fun setOnline(userId: UserId, status: Boolean) {
        val key = buildUserLocationKey(userId)
        if (status) {
            cacheService.set(key, listenTopicCreator.getListenTopic(), TTL)
        } else {
            cacheService.delete(key)
        }
    }

    fun removeActiveChannel(userId: UserId): Boolean {
        val channelIdKey = buildChannelIdKey(userId)

        return try {
            stringRedisTemplate.delete(channelIdKey)
            true
        } catch (ex: Exception) {
            log.error("Redis Delete failed. key: {},", channelIdKey)
            false
        }
    }

    fun refreshTTL(userId: UserId, httpSessionId: String) {
        try {
            val httpSession = httpSessionRepository.findById(httpSessionId)
            if (httpSession != null) {
                httpSession.lastAccessedTime = Instant.now()
                cacheService.expire(buildChannelIdKey(userId), TTL)
                cacheService.expire(buildUserLocationKey(userId), TTL)
            }
        } catch (ex: Exception) {
            log.error("Redis find failed. httpSessionId: {}, cause: {}", httpSessionId, ex.message)
        }
    }

    private fun buildChannelIdKey(userId: UserId): String =
        "%s:%d:%s".format(KeyPrefix.USER, userId.id, IdKey.CHANNEL_ID.value)

    private fun buildUserLocationKey(userId: UserId): String =
        "%s:%d:%s".format(KeyPrefix.USER_SESSION, userId.id.toString())

    companion object {
        private val log = KotlinLogging.logger {}
    }
}