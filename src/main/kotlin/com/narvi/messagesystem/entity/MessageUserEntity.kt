package com.narvi.messagesystem.entity

import jakarta.persistence.*

@Entity
@Table(name = "message_user")
class MessageUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long? = null,
    @Column(name = "username", nullable = false)
    var username: String,
    @Column(name = "password", nullable = false)
    var password: String,
) : BaseTimeEntity() {

}