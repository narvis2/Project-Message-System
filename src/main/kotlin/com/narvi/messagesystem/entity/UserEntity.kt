package com.narvi.messagesystem.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "message_user")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long = 0,
    @Column(name = "username", nullable = false)
    var username: String,
    @Column(name = "password", nullable = false)
    var password: String,
    @Column(name = "connection_invite_code", nullable = false)
    var connectionInviteCode: String = UUID.randomUUID().toString().replace("-", ""),
    @Column(name = "connection_count", nullable = false)
    var connectionCount: Int,
) : BaseTimeEntity() {
}