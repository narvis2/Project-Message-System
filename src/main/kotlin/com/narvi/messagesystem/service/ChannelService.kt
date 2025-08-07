package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.KeyPrefix
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.Channel
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.entity.ChannelEntity
import com.narvi.messagesystem.entity.UserChannelEntity
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.repository.ChannelRepository
import com.narvi.messagesystem.repository.UserChannelRepository
import jakarta.persistence.EntityNotFoundException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val userChannelRepository: UserChannelRepository,
    private val sessionService: SessionService,
    private val userConnectionService: UserConnectionService,
    private val cacheService: CacheService,
    private val jsonUtil: JsonUtil,
) {

    private val TTL: Long = 600 // 10분

    @Transactional(readOnly = true)
    fun getInviteCode(channelId: ChannelId): InviteCode? {
        val key = cacheService.buildKey(KeyPrefix.CHANNEL_INVITECODE, channelId.id.toString())

        val cacheInviteCode = cacheService.get(key)
        if (cacheInviteCode != null) {
            return InviteCode(cacheInviteCode)
        }

        return channelRepository.findChannelInviteCodeByChannelId(channelId.id)?.inviteCode?.let { inviteCode ->
            cacheService.set(key, inviteCode, TTL)
            InviteCode(inviteCode)
        } ?: run {
            log.warn("Invite code is not exist. channelId: {}", channelId)
            null
        }
    }

    @Transactional(readOnly = true)
    fun isJoined(channelId: ChannelId, userId: UserId): Boolean {
        val key = cacheService.buildKey(KeyPrefix.JOINED_CHANNEL, channelId.id.toString(), userId.id.toString())
        val cachedChannel = cacheService.get(key)
        if (cachedChannel != null) {
            return true
        }

        val fromDb = userChannelRepository.existsByUserIdAndChannelId(userId.id, channelId.id)
        if (fromDb) {
            cacheService.set(key, "T", TTL)
        }

        return fromDb
    }

    @Transactional(readOnly = true)
    fun getParticipantIds(channelId: ChannelId): List<UserId> {
        val key = cacheService.buildKey(KeyPrefix.PARTICIPANT_IDS, channelId.id.toString())
        val cachedParticipantIds = cacheService.get(key)
        if (cachedParticipantIds != null) {
            return jsonUtil.fromJsonToList(cachedParticipantIds, String::class.java).map { userId ->
                UserId(userId.toLong())
            }
        }

        val fromDbUserIds = userChannelRepository.findUserIdsByChannelId(
            channelId.id
        ).map { userId ->
            UserId(userId.userId)
        }

        if (fromDbUserIds.isNotEmpty()) {
            jsonUtil.toJson(fromDbUserIds.map { it.id.toString() })?.let { json ->
                cacheService.set(key, json, TTL)
            }
        }

        return fromDbUserIds
    }

    fun getOnlineParticipantIds(channelId: ChannelId, userIds: List<UserId>): List<UserId> =
        sessionService.getOnlineParticipantUserIds(channelId, userIds)

    @Transactional(readOnly = true)
    fun getChannel(inviteCode: InviteCode): Channel? {
        val key = cacheService.buildKey(KeyPrefix.CHANNEL, inviteCode.code)

        val cachedChannel = cacheService.get(key)
        if (cachedChannel != null) {
            return jsonUtil.fromJson(cachedChannel, Channel::class.java)
        }

        val fromDbChannel =  channelRepository.findChannelByInviteCode(inviteCode.code)?.let {
            Channel(
                channelId = ChannelId(id = it.channelId), title = it.title, headCount = it.headCount
            )
        }

        if (fromDbChannel != null) {
            jsonUtil.toJson(fromDbChannel)?.let { json ->
                cacheService.set(key, json, TTL)
            }
        }

        return fromDbChannel
    }

    @Transactional(readOnly = true)
    fun getChannels(userId: UserId): List<Channel> {
        val key = cacheService.buildKey(KeyPrefix.CHANNELS, userId.id.toString())

        val cachedChannels = cacheService.get(key)
        if (cachedChannels != null) {
            return jsonUtil.fromJsonToList(cachedChannels, Channel::class.java)
        }

        val fromDbChannels = userChannelRepository.findChannelsByUserId(userId.id).map {
            Channel(channelId = ChannelId(id = it.channelId), title = it.title, headCount = it.headCount)
        }

        if (fromDbChannels.isNotEmpty()) {
            jsonUtil.toJson(fromDbChannels)?.let { json ->
                cacheService.set(key, json, TTL)
            }
        }

        return fromDbChannels
    }

    @Transactional
    fun create(senderUserId: UserId, participantIds: List<UserId>, title: String): Pair<Channel?, ResultType> {
        if (title.isNullOrBlank()) {
            log.warn("Invalid args : title is empty.")
            return null to ResultType.INVALID_ARGS
        }

        // 여기서 + 1는 본인 (본인은 빠져있음)
        val headCount = participantIds.size + 1
        if (headCount > LIMIT_HEAD_COUNT) {
            log.warn(
                "Over limit channel. senderUserId: {}, participantIds count={}, title={}",
                senderUserId,
                participantIds.size,
                title
            )
            return null to ResultType.OVER_LIMIT
        }

        if (userConnectionService.countConnectionStatus(
                senderUserId, participantIds, UserConnectionStatus.ACCEPTED
            ) != participantIds.size.toLong()
        ) {
            log.warn("Included unconnected user. participantIds: {}", participantIds)
            return null to ResultType.NOT_ALLOWED
        }

        return try {
            val channelEntity = channelRepository.save(
                ChannelEntity(
                    title = title, headCount = headCount
                )
            )
            val channelId = channelEntity.channelId ?: throw IllegalStateException("Channel ID is null after save.")

            val userChannelEntities = participantIds.map { participantId ->
                UserChannelEntity(
                    userId = participantId.id, channelId = channelId, lastReadMsgSeq = 0
                )
            }.toMutableList().apply {
                add(UserChannelEntity(userId = senderUserId.id, channelId = channelId, lastReadMsgSeq = 0))
            }

            userChannelRepository.saveAll(userChannelEntities)
            Channel(ChannelId(id = channelId), title, headCount) to ResultType.SUCCESS
        } catch (ex: Exception) {
            log.error("Create failed. cause: {}", ex.message)
            throw ex
        }
    }

    @Transactional
    fun join(inviteCode: InviteCode, userId: UserId): Pair<Channel?, ResultType> {
        val channel = getChannel(inviteCode) ?: return null to ResultType.NOT_FOUND
        if (isJoined(channel.channelId, userId)) {
            return null to ResultType.ALREADY_JOINED
        }

        if (channel.headCount >= LIMIT_HEAD_COUNT) {
            return null to ResultType.OVER_LIMIT
        }

        val channelEntity = channelRepository.findForUpdateByChannelId(channel.channelId.id)
            ?: throw EntityNotFoundException("Invalid channelId: ${channel.channelId}")

        // Join 가능
        if (channelEntity.headCount < LIMIT_HEAD_COUNT) {
            channelEntity.headCount += 1
            userChannelRepository.save(
                UserChannelEntity(
                    userId = userId.id, channelId = channel.channelId.id, lastReadMsgSeq = 0
                )
            )
            cacheService.delete(
                listOf(
                    cacheService.buildKey(KeyPrefix.CHANNEL, channelEntity.inviteCode),
                    cacheService.buildKey(KeyPrefix.CHANNELS, userId.id.toString()),
                )
            )
        }

        return channel to ResultType.SUCCESS
    }

    @Transactional(readOnly = true)
    fun enter(channelId: ChannelId, userId: UserId): Pair<String?, ResultType> {
        if (!isJoined(channelId, userId)) {
            log.warn("Enter channel failed. User not joined the channel. channelId: {}, userId: {}", channelId, userId)
            return null to ResultType.NOT_JOINED
        }

        val title = channelRepository.findChannelTitleByChannelId(channelId.id)?.title
        if (title.isNullOrBlank()) {
            log.warn("Enter channel failed. Channel not found. channelId: {}, userId: {}", channelId, userId)
            return null to ResultType.NOT_FOUND
        }

        // 레디스에 해당 유저가 어떤 Channel 에 참가했는지에 대한 데이터 저장
        return if (sessionService.setActiveChannel(userId, channelId)) {
            title to ResultType.SUCCESS
        } else {
            log.error("Enter channel failed. channelId: {}, userId: {}", channelId, userId)
            null to ResultType.FAILED
        }
    }

    fun leave(userId: UserId): Boolean = sessionService.removeActiveChannel(userId)

    @Transactional
    fun quit(channelId: ChannelId, userId: UserId): ResultType {
        if (!isJoined(channelId, userId)) {
            return ResultType.NOT_JOINED
        }

        val channelEntity = channelRepository.findForUpdateByChannelId(channelId.id)
            ?: throw EntityNotFoundException("Invalid channelId: $channelId")

        if (channelEntity.headCount > 0) {
            channelEntity.headCount -= 1
        } else {
            log.error("Count is already zero. channelId: {}, userId: {}", channelId, userId)
        }

        userChannelRepository.deleteByUserIdAndChannelId(userId.id, channelId.id)
        cacheService.delete(
            listOf(
                cacheService.buildKey(KeyPrefix.CHANNEL, channelEntity.inviteCode),
                cacheService.buildKey(KeyPrefix.CHANNELS, userId.id.toString()),
            )
        )
        return ResultType.SUCCESS
    }

    companion object {
        private const val LIMIT_HEAD_COUNT = 100
        private val log = KotlinLogging.logger {}
    }
}