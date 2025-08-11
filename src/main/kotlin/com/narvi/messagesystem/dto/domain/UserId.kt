package com.narvi.messagesystem.dto.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class UserId @JsonCreator constructor(@field:JsonValue val id: Long) {
    init {
        require(id > 0) { "Invalid UserId" }
    }
}
