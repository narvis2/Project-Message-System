package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.CountProjection
import com.narvi.messagesystem.dto.projection.InviteCodeProjection
import com.narvi.messagesystem.dto.projection.UserIdProjection
import com.narvi.messagesystem.dto.projection.UsernameProjection
import com.narvi.messagesystem.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface UserRepository : JpaRepository<UserEntity, Long> {
    // Transaction 을 중복으로 사용할때 JPA Cache 오류 수정을 위해 하나 더 만듬
    fun findUserIdByUsername(username: String): UserIdProjection?

    fun findByUsernameIn(usernames: Collection<String>): List<UserIdProjection>

    fun findByUserId(userId: Long): UsernameProjection?

    fun findByInviteCode(inviteCode: String): UserEntity?

    fun findInviteCodeByUserId(userId: Long): InviteCodeProjection?

    fun findCountByUserId(userId: Long): CountProjection?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByUserId(userId: Long): UserEntity?
}