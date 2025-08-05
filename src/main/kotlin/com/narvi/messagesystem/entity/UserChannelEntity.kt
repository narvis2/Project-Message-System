package com.narvi.messagesystem.entity

import jakarta.persistence.*


@Entity
@Table(name = "user_channel")
@IdClass(UserChannelId::class)
class UserChannelEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: Long? = null,
    @Id
    @Column(name = "channel_id", nullable = false)
    var channelId: Long? = null,
    @Column(name = "last_read_msg_seq", nullable = false)
    var lastReadMsgSeq: Long = 0,
) : BaseTimeEntity()