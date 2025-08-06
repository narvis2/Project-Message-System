package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class SessionService(
    private val httpSessionRepository: SessionRepository<out Session>,
    private val stringRedisTemplate: StringRedisTemplate,
) {

    private val TTL: Long = 300

    fun getUsername(): String = SecurityContextHolder.getContext().authentication.name

    fun getOnlineParticipantUserIds(channelId: ChannelId, userIds: List<UserId>): List<UserId?> {
        val channelIdKeys = userIds.map(::buildChannelIdKey)
        try {
            val channelIds = stringRedisTemplate.opsForValue().multiGet(channelIdKeys)
            if (channelIds != null) {
                val onlineParticipantUserIds: MutableList<UserId?> = ArrayList(userIds.size)
                val chId: String = channelId.id.toString()

                for (idx in userIds.indices) {
                    val value = channelIds[idx]
                    onlineParticipantUserIds.add(
                        if (value != null && value == chId) {
                            userIds[idx]
                        } else {
                            null
                        }
                    )
                }

                return onlineParticipantUserIds
            }
        } catch (ex: Exception) {
            log.error("Redis mget failed. key: {}, cause: {}", channelIdKeys, ex.message)
        }

        return emptyList()
    }

    fun setActiveChannel(userId: UserId, channelId: ChannelId): Boolean {
        val channelIdKey = buildChannelIdKey(userId)

        return try {
            stringRedisTemplate.opsForValue().set(channelIdKey, channelId.id.toString(), TTL, TimeUnit.SECONDS)
            true
        } catch (ex: Exception) {
            log.error("Redis set failed. key: {}, channelId: {}", channelIdKey, channelId)
            false
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
        val channelIdKey = buildChannelIdKey(userId)

        try {
            val httpSession = httpSessionRepository.findById(httpSessionId)
            if (httpSession != null) {
                httpSession.lastAccessedTime = Instant.now()
                stringRedisTemplate.expire(channelIdKey, TTL, TimeUnit.SECONDS)
            }
        } catch (ex: Exception) {
            log.error("Redis expire failed. key: {}", channelIdKey)
        }
    }

    private fun buildChannelIdKey(userId: UserId): String {
        val NAMESPACE = "message:user"
        return "%s:%d:%s".format(NAMESPACE, userId.id, IdKey.CHANNEL_ID.value)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}