package com.narvi.messagesystem.config

import com.narvi.messagesystem.auth.WebSocketHttpSessionHandshakeInterceptor
import com.narvi.messagesystem.handler.WebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketHandlerConfig(
    private val webSocketHandler: WebSocketHandler,
    private val webSocketHttpSessionHandshakeInterceptor: WebSocketHttpSessionHandshakeInterceptor
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(webSocketHandler, "/ws/v1/message")
            .addInterceptors(webSocketHttpSessionHandshakeInterceptor)
    }
}