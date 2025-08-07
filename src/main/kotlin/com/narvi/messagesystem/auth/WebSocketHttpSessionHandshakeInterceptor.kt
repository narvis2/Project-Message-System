package com.narvi.messagesystem.auth

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

/**
 * 클라이언트와 서버 간에 WebSocket 연결을 설정하는 초기 단계
 * 이 과정은 HTTP 프로토콜을 통해 이루어지며,
 * Client 의 WebSocket 연결 요청을 서버가 수락하고,
 * 양방향 통신 채널을 확립하는 절차
 */
@Component
class WebSocketHttpSessionHandshakeInterceptor : HttpSessionHandshakeInterceptor() {

    /**
     * httpSession 과 WebSocket Session 이 같이 공존할 수 있는 순간
     */
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        if (request !is ServletServerHttpRequest) {
            log.info("WebSocket handshake failed. request: {}", request.javaClass)
            response.setStatusCode(HttpStatus.BAD_REQUEST)
            return false
        }

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null) {
            log.warn("WebSocket handshake failed. authentication is null")
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        val httpSession = request.servletRequest.getSession(false)
        if (httpSession == null) {
            log.warn("WebSocket handshake failed. httpSession is null")
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        val userId = (authentication.principal as MessageUserDetails).userId
        attributes[IdKey.HTTP_SESSION_ID.value] = httpSession.id
        log.info("getUserId 👉 $userId")
        attributes[IdKey.USER_ID.value] = UserId(userId)
        return true
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}