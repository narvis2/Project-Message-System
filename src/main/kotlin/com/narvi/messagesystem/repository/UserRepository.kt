package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.CountProjection
import com.narvi.messagesystem.dto.projection.InviteCodeProjection
import com.narvi.messagesystem.dto.projection.UsernameProjection
import com.narvi.messagesystem.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?

    fun findByUserId(userId: Long): UsernameProjection?

    fun findByConnectionInviteCode(connectionInviteCode: String): UserEntity?

    fun findInviteCodeByUserId(userId: Long): InviteCodeProjection?

    fun findCountByUserId(userId: Long): CountProjection?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByUserId(userId: Long): UserEntity?
}