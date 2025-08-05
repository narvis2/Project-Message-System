package com.narvi.messagesystem.entity

import jakarta.persistence.*

@Entity
@Table(name = "message")
class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence")
    var messageSequence: Long? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "content", nullable = false)
    var content: String,
) : BaseTimeEntity() {

}