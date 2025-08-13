package com.narvi.messagesystem.database

import mu.KotlinLogging
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Replica 및 Source 처리
 * Message DB의 경우 channelId를 기준으로 모듈러 샤딩 처리
 * - 홀수인 경우 -> Message1
 * - 짝수인 경우 -> Message2
 */
class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        var dataSourceKey =
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) "replica" else "source"

        val channelId = ShardContext.getChannelId()
        if (channelId != null) {
            dataSourceKey += if (channelId % 2L == 0L) "Message2" else "Message1"
        }

        log.info("🚏 Routing to {} dataSource", dataSourceKey)
        return dataSourceKey
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}