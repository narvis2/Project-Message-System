package com.narvi.messagesystem.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SessionService(
    private val httpSessionRepository: SessionRepository<out Session>
) {

    fun getUsername(): String = SecurityContextHolder.getContext().authentication.name

    fun refreshTTL(httpSessionId: String) {
        val httpSession = httpSessionRepository.findById(httpSessionId)
        if (httpSession != null) {
            httpSession.lastAccessedTime = Instant.now()
        }
    }
}