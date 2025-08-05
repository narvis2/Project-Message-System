package com.narvi.messagesystem.constant

enum class IdKey(val value: String) {
    HTTP_SESSION_ID("HTTP_SESSION_ID"),
    USER_ID("USER_ID"),
    CHANNEL_ID("channel_id") // 레디스에 저장될꺼라 소문자 (레디스의 권장 사항은 소문자임)
}