package com.narvi.messagesystem.dto.domain

import com.narvi.messagesystem.constant.UserConnectionStatus

class Connection(val username: String, status: UserConnectionStatus) {
    val status: UserConnectionStatus = status
}