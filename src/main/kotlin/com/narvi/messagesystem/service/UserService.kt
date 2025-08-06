package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.User
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.UserEntity
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
) {

//    @Transactional(readOnly = true)
//    fun getUserId(username: String): UserId? = userRepository.findByUsername(username)?.userId?.let(::UserId)

    @Transactional(readOnly = true)
    fun getUserId(username: String): UserId? = userRepository.findUserIdByUsername(username)?.userId?.let(::UserId)

    @Transactional(readOnly = true)
    fun getUsername(userId: UserId): String? = userRepository.findByUserId(userId.id)?.username

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
        userRepository.deleteById(entity.userId)

        log.info("User unregistered. userId: {}, username: {}", entity.userId, entity.username)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}