package com.narvi.messagesystem.database

import mu.KotlinLogging
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any {
        val dataSourceKey =
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) "replica" else "source"

        log.info("Routing to {} dataSource", dataSourceKey)
        return dataSourceKey
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}