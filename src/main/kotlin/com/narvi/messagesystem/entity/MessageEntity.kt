package com.narvi.messagesystem.entity

import jakarta.persistence.*

@Entity
@Table(name = "message")
class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence", nullable = false)
    var messageSequence: Long? = null,
    @Column(name = "user_name", nullable = false)
    var userName: String,
    @Column(name = "content", nullable = false)
    var content: String,
) : BaseTimeEntity() {

}