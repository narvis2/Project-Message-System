package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.User
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.UserConnectionEntity
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.repository.UserConnectionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class UserConnectionService(
    private val userService: UserService,
    private val userConnectionRepository: UserConnectionRepository,
    private val userConnectionLimitService: UserConnectionLimitService,
    private val cacheService: CacheService,
    private val jsonUtil: JsonUtil
) {

    private val TTL: Long = 600 // 10분

    /**
     * 하나의 함수에서 Query 를 2번 날랄때는 readOnly true 를 해야 함
     * 명시적으로 하나의 Connection Pool 에서 동작함
     */
    @Transactional(readOnly = true)
    fun getUsersByStatus(userId: UserId, status: UserConnectionStatus): List<User> {
        val key = cacheService.buildKey(KeyPrefix.CONNECTIONS_STATUS, userId.id.toString(), status.name)
        log.info("📌 UserConnectionService > getUsersByStatus > $key")
        val cachedUsers = cacheService.get(key)
        if (cachedUsers != null) {
            return jsonUtil.fromJsonToList(cachedUsers, User::class.java)
        }

        val usersA = userConnectionRepository.findByPartnerAUserIdAndStatus(userId.id, status)
        val usersB = userConnectionRepository.findByPartnerBUserIdAndStatus(userId.id, status)

        // Pending 일때 요청 받은 사람만 Pending List 를 볼 수 있도록 수정
        val fromDb = if (status == UserConnectionStatus.ACCEPTED) {
            (usersA + usersB).map { User(UserId(it.userId), it.username) }
        } else {
            (usersA + usersB).filter { item ->
                item.inviterUserId != userId.id
            }.map { User(UserId(it.userId), it.username) }
        }

        if (fromDb.isNotEmpty()) {
            jsonUtil.toJson(fromDb)?.let {
                cacheService.set(key, it, TTL)
            }
        }

        return fromDb
    }

    @Transactional(readOnly = true)
    fun getStatus(
        inviterUserId: UserId, partnerUserId: UserId
    ): UserConnectionStatus {
        val partnerA = minOf(inviterUserId.id, partnerUserId.id)
        val partnerB = maxOf(inviterUserId.id, partnerUserId.id)

        val key = cacheService.buildKey(KeyPrefix.CONNECTION_STATUS, partnerA.toString(), partnerB.toString())

        log.info("📌 UserConnectionService > getStatus > $key")

        val connectionStatus = cacheService.get(key)
        if (connectionStatus != null) {
            return UserConnectionStatus.valueOf(connectionStatus)
        }

        val fromDB = userConnectionRepository.findUserConnectionStatusByPartnerAUserIdAndPartnerBUserId(
            partnerA,
            partnerB,
        )?.status?.let(UserConnectionStatus::valueOf) ?: UserConnectionStatus.NONE

        cacheService.set(key, fromDB.name, TTL)
        return fromDB
    }

    @Transactional(readOnly = true)
    fun countConnectionStatus(senderUserId: UserId, partnerIds: List<UserId>, status: UserConnectionStatus): Long {
        val ids = partnerIds.map { it.id }
        return userConnectionRepository.countByPartnerAUserIdAndPartnerBUserIdInAndStatus(
            senderUserId.id, ids, status
        ) + userConnectionRepository.countByPartnerBUserIdAndPartnerAUserIdInAndStatus(senderUserId.id, ids, status)
    }

    /**
     * 쓰기랑 묶어있는 readOnly Transaction 은 source db 에 접근함
     * return 초대코드의 userId to 받을 사람에게 누가 당신을 초대했는지 이름
     */
    @Transactional
    fun invite(inviterUserId: UserId, inviteCode: InviteCode): Pair<UserId?, String> {
        // 연결할 대상
        val partner = userService.getUser(inviteCode)
        if (partner == null) {
            log.info("Invalid invite code. {}, from {}", inviteCode, inviterUserId)
            return null to "Invalid invite code."
        }

        val partnerUserId = partner.userId
        val partnerUsername = partner.username
        if (partnerUserId == inviterUserId) {
            return null to "Can't self invite."
        }

        val userConnectionStatus = getStatus(inviterUserId, partnerUserId)

        return when (userConnectionStatus) {
            UserConnectionStatus.NONE, UserConnectionStatus.DISCONNECTED -> {
                val isLimitReached = userService.getConnectionCount(inviterUserId)
                    ?.let { it >= userConnectionLimitService.limitConnections } ?: false

                // Limit 된 경우
                if (isLimitReached) return null to "Connection limit reached."

                val inviterUsername = userService.getUsername(inviterUserId) ?: return null to "InviteRequest failed."

                try {
                    setStatus(inviterUserId, partnerUserId, UserConnectionStatus.PENDING)
                    partnerUserId to inviterUsername
                } catch (ex: Exception) {
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
                    }
                    log.error("Set Pending failed. cause: {}", ex.message)
                    null to "InviteRequest failed."
                }
            }

            UserConnectionStatus.ACCEPTED -> {
                partnerUserId to "Already connected with $partnerUsername"
            }

            UserConnectionStatus.PENDING, UserConnectionStatus.REJECTED -> {
                log.info("{} invites {} but does not deliver the invitation request.", inviterUserId, partnerUsername)
                partnerUserId to "Already invited to $partnerUsername"
            }
        }
    }

    @Transactional
    fun accept(acceptorUserId: UserId, inviterUsername: String): Pair<UserId?, String> {
        val inviterUserId = userService.getUserId(inviterUsername) ?: return null to "Invalid username."
        if (acceptorUserId == inviterUserId) {
            return null to "Can't self accept"
        }

        val actualInviter = getInviterUserId(acceptorUserId, inviterUserId)
        if (actualInviter != inviterUserId) return null to "Invalid username."

        val status = getStatus(inviterUserId, acceptorUserId)
        if (status == UserConnectionStatus.ACCEPTED) {
            return null to "Already accepted"
        }

        if (status != UserConnectionStatus.PENDING) {
            return null to "Accept failed."
        }

        val acceptorUsername = userService.getUsername(acceptorUserId)
        if (acceptorUsername == null) {
            log.error("Invalid userId. userId: {}", acceptorUserId)
            return null to "Accept failed."
        }

        return try {
            userConnectionLimitService.accept(acceptorUserId, inviterUserId)
            inviterUserId to acceptorUsername
        } catch (ex: IllegalStateException) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            }
            null to ex.message!!
        } catch (ex: Exception) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            }
            log.error("Accept failed. cause: {}", ex.message)
            null to "Accept failed."
        }
    }

    @Transactional
    fun reject(senderUserId: UserId, inviterUsername: String): Pair<Boolean, String> {
        val inviterUserId = userService.getUserId(inviterUsername) ?: return false to "Reject failed."

        if (inviterUserId == senderUserId) return false to "Reject failed."

        val actualInviter = getInviterUserId(inviterUserId, senderUserId)
        if (actualInviter != inviterUserId) return false to "Reject failed."

        if (getStatus(inviterUserId, senderUserId) != UserConnectionStatus.PENDING) {
            return false to "Reject failed."
        }

        return try {
            setStatus(inviterUserId, senderUserId, UserConnectionStatus.REJECTED)
            true to inviterUsername
        } catch (ex: Exception) {
            log.error("Set rejected failed. cause: {}", ex.message)
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            }
            false to "Reject failed."
        }
    }

    @Transactional
    fun disconnect(senderUserId: UserId, partnerUsername: String): Pair<Boolean, String> {
        val partnerUserId = userService.getUserId(partnerUsername) ?: return false to "Disconnect failed."
        if (senderUserId == partnerUserId) return false to "Disconnect failed."

        return try {
            val status = getStatus(senderUserId, partnerUserId)
            if (status == UserConnectionStatus.ACCEPTED) {
                userConnectionLimitService.disconnect(senderUserId, partnerUserId)
                return true to partnerUsername
            }

            if (status == UserConnectionStatus.REJECTED && getInviterUserId(
                    senderUserId, partnerUserId
                ) == partnerUserId
            ) {
                setStatus(senderUserId, partnerUserId, UserConnectionStatus.DISCONNECTED)
                true to partnerUsername
            } else {
                false to "Disconnect failed."
            }
        } catch (ex: Exception) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            }
            log.error("Disconnect failed. cause: {}", ex.message)
            false to "Disconnect failed."
        }
    }

    @Transactional(readOnly = true)
    fun getInviterUserId(partnerAUserId: UserId, partnerBUserId: UserId): UserId? {
        val partnerA = minOf(partnerAUserId.id, partnerBUserId.id)
        val partnerB = maxOf(partnerAUserId.id, partnerBUserId.id)

        val key = cacheService.buildKey(KeyPrefix.INVITER_USER_ID, partnerA.toString(), partnerB.toString())
        val cachedInviteUserId = cacheService.get(key)
        if (cachedInviteUserId != null) {
            return UserId(cachedInviteUserId.toLong())
        }

        return userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(
            partnerA,
            partnerB,
        )?.inviterUserId?.let { inviteUserId ->
            cacheService.set(key, inviteUserId.toString(), TTL)
            UserId(inviteUserId)
        }
    }

    @Transactional
    fun setStatus(
        inviterUserId: UserId, partnerUserId: UserId, status: UserConnectionStatus
    ) {
        if (status == UserConnectionStatus.ACCEPTED) {
            throw IllegalArgumentException("Can't set to accepted.")
        }

        val partnerA = minOf(inviterUserId.id, partnerUserId.id)
        val partnerB = maxOf(inviterUserId.id, partnerUserId.id)

        userConnectionRepository.save(
            UserConnectionEntity(
                partnerAUserId = partnerA,
                partnerBUserId = partnerB,
                status = status,
                inviterUserId = inviterUserId.id,
            )
        )
        cacheService.delete(
            listOf(
                cacheService.buildKey(
                    KeyPrefix.CONNECTION_STATUS, partnerA.toString(), partnerB.toString()
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, inviterUserId.id.toString(), status.name
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, partnerUserId.id.toString(), status.name
                ),
            )
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}