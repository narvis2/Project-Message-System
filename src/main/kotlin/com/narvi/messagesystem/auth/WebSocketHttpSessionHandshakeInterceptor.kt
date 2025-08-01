package com.narvi.messagesystem.auth

import com.narvi.messagesystem.constant.Constants
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

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

        request.servletRequest.getSession(false)?.let {
            attributes[Constants.HTTP_SESSION_ID.value] = it.id
            return true
        }

        log.info("WebSocket handshake failed. httpSession is null")
        response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return false
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}