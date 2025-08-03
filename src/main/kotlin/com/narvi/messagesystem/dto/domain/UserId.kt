package com.narvi.messagesystem.dto.domain

data class UserId(val id: Long) {
    init {
        require(id > 0) { "Invalid UserId" }
    }
}
