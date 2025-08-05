package com.narvi.messagesystem.dto.domain

data class ChannelId(val id: Long) {
    init {
        require(id > 0) { "Invalid ChannelId: $id" }
    }
}
