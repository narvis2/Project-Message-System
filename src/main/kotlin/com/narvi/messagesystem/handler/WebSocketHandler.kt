package com.narvi.messagesystem.handler

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.BaseRequest
import com.narvi.messagesystem.handler.websocket.RequestDispatcher
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.session.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class WebSocketHandler(
    private val requestDispatcher: RequestDispatcher,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val jsonUtil: JsonUtil,
) : TextWebSocketHandler() {

    /**
     * 클라이언트와 WebSocket 연결이 성립되었을 때 호출됨.
     * 세션을 데코레이션한 후 userId를 기준으로 세션을 등록함.
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("ConnectionEstablished : {} ", session.id)
        // 5초 안에 발생이 안끝나면 뭔가 문제가 발생한 것으로 보고 Socket 을 끊어 버림
        // Thread Safe
        val concurrentWebSocketSessionDecorator = ConcurrentWebSocketSessionDecorator(
            session,
            5000,
            100 * 1024
        )

        val userId = session.attributes[IdKey.USER_ID.value] as? UserId ?: return
        webSocketSessionManager.putSession(userId, concurrentWebSocketSessionDecorator)
    }

    /**
     * WebSocket 통신 중 오류가 발생했을 때 호출됨.
     * 해당 userId의 세션을 종료함.
     */
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Transport Error : [{}] from {}", exception.message, session.id)
        val userId = session.attributes[IdKey.USER_ID.value] as? UserId ?: return
        webSocketSessionManager.closeSession(userId)
    }

    /**
     * WebSocket 연결이 정상적으로 종료되었을 때 호출됨.
     * 세션 매니저에서 해당 userId의 세션을 제거함.
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes[IdKey.USER_ID.value] as? UserId ?: return
        webSocketSessionManager.closeSession(userId)
    }

    /**
     * 클라이언트로부터 텍스트 메시지를 수신했을 때 호출됨.
     * 메시지를 JSON으로 파싱한 후 적절한 handler에게 위임하여 처리함.
     */
    override fun handleTextMessage(senderSession: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        log.info("Received TextMessage: [{}] from {}", payload, senderSession.id)

        jsonUtil.fromJson(payload, BaseRequest::class.java)?.let {
            requestDispatcher.dispatchRequest(senderSession, it)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}