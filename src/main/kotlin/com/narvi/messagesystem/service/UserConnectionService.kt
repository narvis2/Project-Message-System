package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.User
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.UserConnectionEntity
import com.narvi.messagesystem.repository.UserConnectionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserConnectionService(
    private val userService: UserService,
    private val userConnectionRepository: UserConnectionRepository,
    private val userConnectionLimitService: UserConnectionLimitService,
) {

    fun getUsersByStatus(userId: UserId, status: UserConnectionStatus): List<User> {
        val usersA = userConnectionRepository.findByPartnerAUserIdAndStatus(userId.id, status)
        val usersB = userConnectionRepository.findByPartnerBUserIdAndStatus(userId.id, status)

        // Pending 일때 요청 받은 사람만 Pending List 를 볼 수 있도록 수정
        return if (status == UserConnectionStatus.ACCEPTED) {
            (usersA + usersB)
                .map { User(UserId(it.userId), it.username) }
        } else {
            (usersA + usersB)
                .filter { item ->
                    item.inviterUserId != userId.id
                }
                .map { User(UserId(it.userId), it.username) }
        }
    }

    // return 초대코드의 userId to 받을 사람에게 누가 당신을 초대했는지 이름
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
            null to ex.message!!
        } catch (ex: Exception) {
            log.error("Accept failed. cause: {}", ex.message)
            null to "Accept failed."
        }
    }

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
            false to "Reject failed."
        }
    }

    fun disconnect(senderUserId: UserId, partnerUsername: String): Pair<Boolean, String> {
        val partnerUserId = userService.getUserId(partnerUsername) ?: return false to "Disconnect failed."
        if (senderUserId == partnerUserId) return false to "Disconnect failed."

        return try {
            val status = getStatus(senderUserId, partnerUserId)
            if (status == UserConnectionStatus.ACCEPTED) {
                userConnectionLimitService.disconnect(senderUserId, partnerUserId)
                return true to partnerUsername
            }

            if (
                status == UserConnectionStatus.REJECTED &&
                getInviterUserId(senderUserId, partnerUserId) == partnerUserId
            ) {
                setStatus(senderUserId, partnerUserId, UserConnectionStatus.DISCONNECTED)
                true to partnerUsername
            } else {
                false to "Disconnect failed."
            }
        } catch (ex: Exception) {
            log.error("Disconnect failed. cause: {}", ex.message)
            false to "Disconnect failed."
        }
    }

    private fun getInviterUserId(partnerAUserId: UserId, partnerBUserId: UserId): UserId? =
        userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(
            minOf(partnerAUserId.id, partnerBUserId.id),
            maxOf(partnerAUserId.id, partnerBUserId.id),
        )?.inviterUserId?.let(::UserId)

    private fun getStatus(
        inviterUserId: UserId,
        partnerUserId: UserId
    ): UserConnectionStatus = userConnectionRepository.findUserConnectionStatusByPartnerAUserIdAndPartnerBUserId(
        minOf(inviterUserId.id, partnerUserId.id),
        maxOf(inviterUserId.id, partnerUserId.id),
    )?.status?.let(UserConnectionStatus::valueOf) ?: UserConnectionStatus.NONE

    @Transactional
    fun setStatus(
        inviterUserId: UserId,
        partnerUserId: UserId,
        status: UserConnectionStatus
    ) {
        if (status == UserConnectionStatus.ACCEPTED) {
            throw IllegalArgumentException("Can't set to accepted.")
        }

        userConnectionRepository.save(
            UserConnectionEntity(
                partnerAUserId = minOf(inviterUserId.id, partnerUserId.id),
                partnerBUserId = maxOf(inviterUserId.id, partnerUserId.id),
                status = status,
                inviterUserId = inviterUserId.id,
            )
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}