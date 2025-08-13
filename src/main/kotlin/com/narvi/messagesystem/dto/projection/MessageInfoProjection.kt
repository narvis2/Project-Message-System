package com.narvi.messagesystem.dto.projection

interface MessageInfoProjection {
    val messageSequence: Long
    val userId: Long
    val content: String
}