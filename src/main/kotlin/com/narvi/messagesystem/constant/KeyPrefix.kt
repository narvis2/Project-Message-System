package com.narvi.messagesystem.constant

class KeyPrefix {
    companion object {
        const val USER_SESSION = "message:user_session"
        const val USER = "message:user"
        const val USERNAME = "message:username"
        const val USER_ID = "message:user_id"
        const val USER_INVITECODE = "message:user_invitecode"

        const val CONNECTION_STATUS = "message:connection:status"
        const val CONNECTIONS_STATUS = "message:connections:status"
        const val INVITER_USER_ID = "message:connection:inviter_id"

        const val CHANNEL = "message:channel"
        const val CHANNELS = "message:channels"
        const val CHANNEL_INVITECODE = "message:channel_invitecode"
        const val JOINED_CHANNEL = "message:joined_channel"
        const val PARTICIPANT_IDS = "message:participant_ids"
    }
}