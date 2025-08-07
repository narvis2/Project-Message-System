package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.User
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.UserEntity
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.repository.UserRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val sessionService: SessionService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cacheService: CacheService,
    private val jsonUtil: JsonUtil
) {

    /**
     * 1시간
     * username 바꾸는 기능이 없어서 길게 잡음
     */
    private val TTL: Long = 3600

//    @Transactional(readOnly = true)
//    fun getUserId(username: String): UserId? = userRepository.findByUsername(username)?.userId?.let(::UserId)

    @Transactional(readOnly = true)
    fun getUserId(username: String): UserId? {
        val key = cacheService.buildKey(KeyPrefix.USER_ID, username)

        val cachedUserId = cacheService.get(key)
        if (cachedUserId != null) {
            return UserId(cachedUserId.toLong())
        }

        return userRepository.findUserIdByUsername(username)?.userId?.let { userId ->
            cacheService.set(key, userId.toString(), TTL)
            UserId(userId)
        }
    }

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
    fun getUserIds(usernames: List<String>): List<UserId> =
        userRepository.findByUsernameIn(usernames).map { projection ->
            UserId(projection.userId)
        }

    @Transactional(readOnly = true)
    fun getUser(inviteCode: InviteCode): User? {
        val key = cacheService.buildKey(KeyPrefix.USER, inviteCode.code)

        val cacheUser = cacheService.get(key)
        if (cacheUser != null) {
            return jsonUtil.fromJson(cacheUser, User::class.java)
        }

        return userRepository.findByInviteCode(inviteCode.code)?.let { entity ->
            val payload = jsonUtil.toJson(entity)
            if (payload != null) {
                cacheService.set(key, payload, TTL)
            }

            User(
                userId = UserId(entity.userId),
                username = entity.username,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getInviteCode(userId: UserId): InviteCode? {
        val key = cacheService.buildKey(KeyPrefix.USER_INVITECODE, userId.id.toString())

        val cacheInviteCode = cacheService.get(key)
        if (cacheInviteCode != null) {
            return InviteCode(cacheInviteCode)
        }

        return userRepository.findInviteCodeByUserId(
            userId.id
        )?.inviteCode?.let {
            cacheService.set(key, it, TTL)
            InviteCode(it)
        }
    }

    @Transactional(readOnly = true)
    fun getConnectionCount(userId: UserId): Int? = userRepository.findCountByUserId(
        userId.id
    )?.connectionCount

    @Transactional
    fun addUser(username: String, password: String): UserId {
        val entity = userRepository.save(
            UserEntity(
                username = username,
                password = passwordEncoder.encode(password),
                connectionCount = 0,
            )
        )
        log.info("User registered. userId: {}, username: {}", entity.userId, entity.username)
        return UserId(entity.userId)
    }

    @Transactional
    fun removeUser() {
        val username = sessionService.getUsername()
        val entity = userRepository.findByUsername(username) ?: throw NoSuchElementException()
        val userId = entity.userId.toString()
        userRepository.deleteById(entity.userId)

        cacheService.delete(
            listOf(
                cacheService.buildKey(KeyPrefix.USER_ID, username),
                cacheService.buildKey(KeyPrefix.USERNAME, userId),
                cacheService.buildKey(KeyPrefix.USER, userId),
                cacheService.buildKey(KeyPrefix.USER_INVITECODE, userId),
            )
        )

        log.info("User unregistered. userId: {}, username: {}", entity.userId, entity.username)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}