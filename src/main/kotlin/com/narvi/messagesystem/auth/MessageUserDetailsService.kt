package com.narvi.messagesystem.auth

import com.narvi.messagesystem.repository.MessageUserRepository
import mu.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class MessageUserDetailsService(
    private val messageUserRepository: MessageUserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val name = username ?: throw UsernameNotFoundException("")
        val messageUserEntity = messageUserRepository.findByUsername(name).orElseThrow {
            log.info("User {} not found", name)
            UsernameNotFoundException("")
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