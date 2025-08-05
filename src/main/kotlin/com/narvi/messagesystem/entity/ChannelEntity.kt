package com.narvi.messagesystem.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "channel")
class ChannelEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    var channelId: Long? = null,
    @Column(name = "title", nullable = false)
    var title: String,
    @Column(name = "invite_code", nullable = false)
    var inviteCode: String = UUID.randomUUID().toString().replace("-", ""),
    @Column(name = "head_count", nullable = false)
    var headCount: Int = 0,
) : BaseTimeEntity() {
}