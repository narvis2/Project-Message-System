package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.repository.UserConnectionRepository
import com.narvi.messagesystem.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sun.security.krb5.KrbException.errorMessage

@Service
class UserConnectionLimitService(
    private val userRepository: UserRepository,
    private val userConnectionRepository: UserConnectionRepository,
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
            firstUserId,
            secondUserId,
            UserConnectionStatus.PENDING
        ) ?: throw EntityNotFoundException("Invalid status.")

        fun errorMessage(userId: Long): String =
            if (userId == acceptorUserId.id) "Connection limit reached."
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
            firstUserId,
            secondUserId,
            UserConnectionStatus.ACCEPTED
        ) ?: throw EntityNotFoundException("Invalid status.")

        check(firstUser.connectionCount > 0) { "Count is already zero. userId: $firstUserId" }
        check(secondUser.connectionCount > 0) { "Count is already zero. userId: $secondUserId" }

        firstUser.connectionCount -= 1
        secondUser.connectionCount -= 1
        connection.status = UserConnectionStatus.DISCONNECTED
    }
}