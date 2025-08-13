package com.narvi.messagesystem.dto.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class MessageSeqId @JsonCreator constructor(@field:JsonValue val id: Long)
