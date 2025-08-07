package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.repository.UserConnectionRepository
import com.narvi.messagesystem.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserConnectionLimitService(
    private val userRepository: UserRepository,
    private val userConnectionRepository: UserConnectionRepository,
    private val cacheService: CacheService,
) {

    var limitConnections: Int = 1000

    /**
     * 친구 요청 수락 처리.
     * 두 유저의 연결 수를 증가시키고, 상태를 ACCEPTED로 변경.
     * 제한을 초과하면 예외 발생.
     */
    @Transactional
    fun accept(acceptorUserId: UserId, inviterUserId: UserId) {
        val firstUserId = minOf(acceptorUserId.id, inviterUserId.id)
        val secondUserId = maxOf(acceptorUserId.id, inviterUserId.id)

        val firstUserEntity = userRepository.findForUpdateByUserId(firstUserId)
            ?: throw EntityNotFoundException("Invalid userId: $firstUserId")
        val secondUserEntity = userRepository.findForUpdateByUserId(secondUserId)
            ?: throw EntityNotFoundException("Invalid userId: $secondUserId")

        val connection = userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(
            firstUserId, secondUserId, UserConnectionStatus.PENDING
        ) ?: throw EntityNotFoundException("Invalid status.")

        fun errorMessage(userId: Long): String = if (userId == acceptorUserId.id) "Connection limit reached."
        else "Connection limit reached by the other user."

        check(firstUserEntity.connectionCount < limitConnections) {
            errorMessage(firstUserId)
        }
        check(secondUserEntity.connectionCount < limitConnections) {
            errorMessage(secondUserId)
        }

        firstUserEntity.connectionCount += 1
        secondUserEntity.connectionCount += 1
        connection.status = UserConnectionStatus.ACCEPTED

        val statusKey = cacheService.buildKey(
            KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString()
        )
        val connectionsStatusKey1 = cacheService.buildKey(
            KeyPrefix.CONNECTIONS_STATUS, acceptorUserId.id.toString(), UserConnectionStatus.ACCEPTED.name
        )
        val connectionsStatusKey2 = cacheService.buildKey(
            KeyPrefix.CONNECTIONS_STATUS, inviterUserId.id.toString(), UserConnectionStatus.ACCEPTED.name
        )

        log.info(
            "📌 UserConnectionLimitService > accept > statusKey: {}, connectionsStatusKey1: {}, connectionsStatusKey2: {}, connectionsStatusKey3: {}",
            statusKey,
            connectionsStatusKey1,
            connectionsStatusKey2
        )

        cacheService.delete(
            listOf(
                cacheService.buildKey(
                    KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString()
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, acceptorUserId.id.toString(), UserConnectionStatus.ACCEPTED.name
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, inviterUserId.id.toString(), UserConnectionStatus.ACCEPTED.name
                ),
            )
        )
    }

    @Transactional
    fun disconnect(senderUserId: UserId, partnerUserId: UserId) {
        val firstUserId = minOf(senderUserId.id, partnerUserId.id)
        val secondUserId = maxOf(senderUserId.id, partnerUserId.id)

        val firstUser = userRepository.findForUpdateByUserId(firstUserId)
            ?: throw EntityNotFoundException("Invalid userId: $firstUserId")
        val secondUser = userRepository.findForUpdateByUserId(secondUserId)
            ?: throw EntityNotFoundException("Invalid userId: $secondUserId")

        val connection = userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(
            firstUserId, secondUserId, UserConnectionStatus.ACCEPTED
        ) ?: throw EntityNotFoundException("Invalid status.")

        check(firstUser.connectionCount > 0) { "Count is already zero. userId: $firstUserId" }
        check(secondUser.connectionCount > 0) { "Count is already zero. userId: $secondUserId" }

        firstUser.connectionCount -= 1
        secondUser.connectionCount -= 1
        connection.status = UserConnectionStatus.DISCONNECTED

        val statusKey = cacheService.buildKey(
            KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString()
        )
        val connectionsStatusKey1 = cacheService.buildKey(
            KeyPrefix.CONNECTIONS_STATUS, senderUserId.id.toString(), UserConnectionStatus.DISCONNECTED.name
        )
        val connectionsStatusKey2 = cacheService.buildKey(
            KeyPrefix.CONNECTIONS_STATUS, partnerUserId.id.toString(), UserConnectionStatus.DISCONNECTED.name
        )

        log.info(
            "📌 UserConnectionLimitService > disconnect > statusKey: {}, connectionsStatusKey1: {}, connectionsStatusKey2: {}, connectionsStatusKey3: {}",
            statusKey,
            connectionsStatusKey1,
            connectionsStatusKey2
        )

        cacheService.delete(
            listOf(
                cacheService.buildKey(
                    KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString()
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, senderUserId.id.toString(), UserConnectionStatus.DISCONNECTED.name
                ),
                cacheService.buildKey(
                    KeyPrefix.CONNECTIONS_STATUS, partnerUserId.id.toString(), UserConnectionStatus.DISCONNECTED.name
                ),
            )
        )
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}