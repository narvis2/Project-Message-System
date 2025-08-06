package com.narvi.messagesystem.auth

import com.narvi.messagesystem.repository.UserRepository
import mu.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MessageUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String?): UserDetails {
        val name = username ?: throw UsernameNotFoundException("")
        val messageUserEntity = userRepository.findByUsername(name)
        if (messageUserEntity == null) {
            log.info("User {} not found", name)
            throw UsernameNotFoundException("")
        }

        val userId = messageUserEntity.userId ?: throw UsernameNotFoundException("")

        return MessageUserDetails(
            userId,
            messageUserEntity.username,
            messageUserEntity.password,
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}