package com.narvi.messagesystem.handler.kafka

import com.narvi.messagesystem.dto.kafka.BaseRecord

interface BaseRecordHandler<T : BaseRecord> {
    fun handleRecord(record: T)
}