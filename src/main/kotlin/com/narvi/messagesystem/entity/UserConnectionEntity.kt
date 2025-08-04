package com.narvi.messagesystem.entity

import com.narvi.messagesystem.constant.UserConnectionStatus
import jakarta.persistence.*


@Entity
@Table(name = "user_connection")
@IdClass(UserConnectionId::class)
class UserConnectionEntity(
    @Id
    @Column(name = "partner_a_user_id", nullable = false)
    var partnerAUserId: Long? = null,
    @Id
    @Column(name = "partner_b_user_id", nullable = false)
    var partnerBUserId: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserConnectionStatus,
    @Column(name = "inviter_user_id", nullable = false)
    var inviterUserId: Long,
) : BaseTimeEntity()