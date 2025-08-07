package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import mu.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SessionService(
    private val httpSessionRepository: SessionRepository<out Session>,
    private val cacheService: CacheService,
) {

    private val TTL: Long = 300

    fun getUsername(): String = SecurityContextHolder.getContext().authentication.name

    fun getOnlineParticipantUserIds(channelId: ChannelId, userIds: List<UserId>): List<UserId> {
        val channelIdKeys = userIds.map(::buildChannelIdKey)
        val channelIds = cacheService.get(channelIdKeys)
        if (channelIds.isNotEmpty()) {
            val onlineParticipantUserIds: MutableList<UserId> = ArrayList(userIds.size)
            val chId: String = channelId.id.toString()

            for (idx in userIds.indices) {
                val value = channelIds[idx]
                if (value != null && value == chId) {
                    onlineParticipantUserIds.add(userIds[idx])
                }
            }

            return onlineParticipantUserIds
        }

        return emptyList()
    }

    fun setActiveChannel(userId: UserId, channelId: ChannelId): Boolean =
        cacheService.set(buildChannelIdKey(userId), channelId.id.toString(), TTL)

    fun removeActiveChannel(userId: UserId): Boolean = cacheService.delete(buildChannelIdKey(userId))

    fun refreshTTL(userId: UserId, httpSessionId: String) {
        val channelIdKey = buildChannelIdKey(userId)

        try {
            val httpSession = httpSessionRepository.findById(httpSessionId)
            if (httpSession != null) {
                httpSession.lastAccessedTime = Instant.now()
                cacheService.expire(channelIdKey, TTL)
            }
        } catch (ex: Exception) {
            log.error("Redis find failed. httpSessionId: {}, cause: {}", httpSessionId, ex.message)
        }
    }

    private fun buildChannelIdKey(userId: UserId): String =
        cacheService.buildKey(KeyPrefix.USER, userId.id.toString(), IdKey.CHANNEL_ID.value)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}