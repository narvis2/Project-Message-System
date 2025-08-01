package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.MessageUserEntity
import com.narvi.messagesystem.repository.MessageUserRepository
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageUserService(
    private val sessionService: SessionService,
    private val messageUserRepository: MessageUserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun addUser(username: String, password: String): UserId {
        val entity = messageUserRepository.save(
            MessageUserEntity(
                username = username,
                password = passwordEncoder.encode(password)
            )
        )
        log.info("User registered. userId: {}, username: {}", entity.userId, entity.username)
        return UserId(entity.userId ?: 0)
    }

    @Transactional
    fun removeUser() {
        val username = sessionService.getUsername()
        val entity = messageUserRepository.findByUsername(username).orElseThrow()
        entity.userId?.let {
            messageUserRepository.deleteById(it)
        }

        log.info("User unregistered. userId: {}, username: {}", entity.userId, entity.username)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}