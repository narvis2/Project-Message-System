package com.narvi.messagesystem.entity

import jakarta.persistence.*

@Entity
@Table(name = "message")
@IdClass(ChannelSequenceId::class)
class MessageEntity(
    @Id
    @Column(name = "channel_id", nullable = false)
    var channelId: Long? = null,
    @Id
    @Column(name = "message_sequence", nullable = false)
    var messageSequence: Long? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "content", nullable = false)
    var content: String,
) : BaseTimeEntity() {

}