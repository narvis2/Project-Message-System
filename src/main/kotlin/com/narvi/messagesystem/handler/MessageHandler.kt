package com.narvi.messagesystem.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.narvi.messagesystem.dto.Message
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.repository.MessageRepository
import com.narvi.messagesystem.session.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class MessageHandler(
    private val sessionManager: WebSocketSessionManager,
    private val messageRepository: MessageRepository,
) : TextWebSocketHandler() {

    private val objectMapper = ObjectMapper().apply {
        registerModules(JavaTimeModule())
        registerModules(kotlinModule())
        registerModule(ParameterNamesModule())
        activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Any::class.java)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
        )
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("ConnectionEstablished : {} ", session.id)
        // 5초 안에 발생이 안끝나면 뭔가 문제가 발생한 것으로 보고 Socket 을 끊어 버림
        // Thread Safe
        val concurrentWebSocketSessionDecorator = ConcurrentWebSocketSessionDecorator(
            session,
            5000,
            100 * 1024
        )
        sessionManager.storeSession(concurrentWebSocketSessionDecorator)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Transport Error : [{}] from {}", exception.message, session.id)
        sessionManager.terminateSession(session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("ConnectionClosed : [{}] from {}", status, session.id)
        sessionManager.terminateSession(session.id)
    }

    override fun handleTextMessage(senderSession: WebSocketSession, message: TextMessage) {
        log.info("Received TextMessage: [{}] from {}", message, senderSession.id)

        val payload = message.payload

        try {
            val receivedMessage = objectMapper.readValue(payload, Message::class.java)
            messageRepository.save(
                MessageEntity(
                    userName = receivedMessage.username,
                    content = receivedMessage.content
                )
            )

            sessionManager.getSessions().forEach {  participantSession ->
                if (senderSession.id != participantSession.id) {
                    sendMessage(participantSession, receivedMessage)
                }
            }
        } catch (e: Exception) {
            val errorMsg = "유효한 프로토콜이 아닙니다."
            log.error("errorMessage payload: {}", payload, senderSession.id)
            sendMessage(senderSession, Message("system", errorMsg))
        }
    }

    private fun sendMessage(session: WebSocketSession, message: Message) {
        try {
            val msg = objectMapper.writeValueAsString(message)
            session.sendMessage(TextMessage(msg))
            log.info("send message: {} to {}", msg, session.id)
        } catch (e: Exception) {
            log.error("메시지 전송 실패 to {} error: {}", session.id, e.message)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}