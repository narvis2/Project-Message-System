package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.BaseRecord
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType

@Component
class RecordDispatcher(
    private val listableBeanFactory: ListableBeanFactory
) {

    private val handlerMap: MutableMap<Class<out BaseRecord>, BaseRecordHandler<out BaseRecord>> = mutableMapOf()

    fun <T : BaseRecord> dispatchRecord(
        record: T
    ) {
        val handler = handlerMap[record::class.java] as? BaseRecordHandler<T>
        if (handler != null) {
            handler.handleRecord(record)
        } else {
            log.error("Handler not found for record type: {}", record::class.simpleName)
        }
    }

    @PostConstruct
    private fun prepareRecordHandlerMapping() {
        val beanHandlers = listableBeanFactory.getBeansOfType(BaseRecordHandler::class.java)
        for (handler in beanHandlers.values) {
            val recordClass = extractRecordClass(handler)
            if (recordClass != null) {
                @Suppress("UNCHECKED_CAST")
                handlerMap[recordClass] = handler as BaseRecordHandler<out BaseRecord>
            }
        }
    }

    private fun extractRecordClass(handler: BaseRecordHandler<*>): Class<out BaseRecord>? {
        for (type in handler.javaClass.genericInterfaces) {
            if (type is ParameterizedType && type.rawType == BaseRecordHandler::class.java) {
                @Suppress("UNCHECKED_CAST")
                return type.actualTypeArguments[0] as? Class<out BaseRecord>
            }
        }

        return null
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}