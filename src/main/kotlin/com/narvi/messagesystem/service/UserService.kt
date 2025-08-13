package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.User
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val cacheService: CacheService
) {

//    @Transactional(readOnly = true)
//    fun getUserId(username: String): UserId? = userRepository.findByUsername(username)?.userId?.let(::UserId)

    @Transactional(readOnly = true)
    fun getUserId(username: String): UserId? = userRepository.findUserIdByUsername(username)?.userId?.let(::UserId)

    @Transactional(readOnly = true)
    fun getUsername(userId: UserId): String? {
        val key = cacheService.buildKey(KeyPrefix.USERNAME, userId.id.toString())

        val cachedUsername = cacheService.get(key)
        if (cachedUsername != null) {
            return cachedUsername
        }

        return userRepository.findByUserId(userId.id)?.username?.let { username ->
            cacheService.set(key, username, TTL)
            username
        }
    }


    @Transactional(readOnly = true)
    fun getUsernames(userIds: Set<UserId>): Pair<Map<UserId, String>, ResultType> {
        if (userIds.size > LIMIT_FIND_COUNT) {
            return emptyMap<UserId, String>() to ResultType.OVER_LIMIT
        }

        val usernames = cacheService.get(
            userIds.map { userId -> cacheService.buildKey(KeyPrefix.USERNAME, userId.id.toString()) }
        )

        val resultMap = mutableMapOf<UserId, String>()
        val missingUserIds = mutableSetOf<UserId>()

        userIds.forEachIndexed { index, userId ->
            val username = usernames[index]
            if (username != null) {
                resultMap[userId] = username
            } else {
                missingUserIds.add(userId)
            }
        }

        if (missingUserIds.isNotEmpty()) {
            val userIdsAndUsernames = userRepository.findByUserIdIn(missingUserIds.map { it.id }.toSet()).associate {
                UserId(it.userId) to it.username
            }

            resultMap.putAll(userIdsAndUsernames)
            cacheService.set(
                userIdsAndUsernames.mapKeys {
                    cacheService.buildKey(KeyPrefix.USERNAME, it.key.id.toString())
                },
                TTL
            )
        }

        return resultMap to ResultType.SUCCESS
    }

    @Transactional(readOnly = true)
    fun getUserIds(usernames: List<String>): List<UserId> =
        userRepository.findByUsernameIn(usernames).map { projection ->
            UserId(projection.userId)
        }

    @Transactional(readOnly = true)
    fun getUser(inviteCode: InviteCode): User? = userRepository.findByInviteCode(inviteCode.code)?.let { entity ->
        User(
            userId = UserId(entity.userId),
            username = entity.username,
        )
    }

    @Transactional(readOnly = true)
    fun getInviteCode(userId: UserId): InviteCode? = userRepository.findInviteCodeByUserId(
        userId.id
    )?.inviteCode?.let(::InviteCode)

    @Transactional(readOnly = true)
    fun getConnectionCount(userId: UserId): Int? = userRepository.findCountByUserId(
        userId.id
    )?.connectionCount

    companion object {
        private val log = KotlinLogging.logger {}
        private const val TTL: Long = 3600
        private const val LIMIT_FIND_COUNT: Long = 100
    }
}