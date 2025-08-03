package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.dto.websocket.inbound.BaseRequest
import org.springframework.web.socket.WebSocketSession

interface BaseRequestHandler<T : BaseRequest> {
    fun handleRequest(webSocketSession: WebSocketSession, request: T)
}