package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.dto.websocket.inbound.BaseRequest
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.lang.reflect.ParameterizedType

@Component
class RequestDispatcher(
    private val listableBeanFactory: ListableBeanFactory
) {
    private val handlerMap: MutableMap<Class<out BaseRequest>, BaseRequestHandler<out BaseRequest>> = mutableMapOf()

    fun <T : BaseRequest> dispatchRequest(
        webSocketSession: WebSocketSession,
        request: T
    ) {
        val handler = handlerMap[request::class.java] as? BaseRequestHandler<T>
        if (handler != null) {
            handler.handleRequest(webSocketSession, request)
        } else {
            log.error("Handler not found for request type: {}", request::class.simpleName)
        }
    }

    @PostConstruct
    private fun prepareRequestHandlerMapping() {
        val beanHandlers = listableBeanFactory.getBeansOfType(BaseRequestHandler::class.java)
        for (handler in beanHandlers.values) {
            val requestClass = extractRequestClass(handler)
            if (requestClass != null) {
                @Suppress("UNCHECKED_CAST")
                handlerMap[requestClass] = handler as BaseRequestHandler<out BaseRequest>
            }
        }
    }

    private fun extractRequestClass(handler: BaseRequestHandler<*>): Class<out BaseRequest>? {
        for (type in handler.javaClass.genericInterfaces) {
            if (type is ParameterizedType && type.rawType == BaseRequestHandler::class.java) {
                @Suppress("UNCHECKED_CAST")
                return type.actualTypeArguments[0] as? Class<out BaseRequest>
            }
        }

        return null
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}