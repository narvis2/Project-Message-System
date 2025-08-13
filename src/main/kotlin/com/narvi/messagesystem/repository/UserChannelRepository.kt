package com.narvi.messagesystem.repository

import com.narvi.messagesystem.dto.projection.ChannelProjection
import com.narvi.messagesystem.dto.projection.LastReadMsgSeqProjection
import com.narvi.messagesystem.dto.projection.UserIdProjection
import com.narvi.messagesystem.entity.UserChannelEntity
import com.narvi.messagesystem.entity.UserChannelId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserChannelRepository : JpaRepository<UserChannelEntity, UserChannelId> {

    fun existsByUserIdAndChannelId(userId: Long, channelId: Long): Boolean

    fun findUserIdsByChannelId(channelId: Long): List<UserIdProjection>

    @Query(
        """
            SELECT c.channelId AS channelId, c.title AS title, c.headCount AS headCount
            FROM UserChannelEntity uc
            INNER JOIN ChannelEntity c ON uc.channelId = c.channelId
            WHERE uc.userId = :userId
        """
    )
    fun findChannelsByUserId(@Param("userId") userId: Long): List<ChannelProjection>

    fun findLastReadMsgSeqByUserIdAndChannelId(userId: Long, channelId: Long): LastReadMsgSeqProjection?

    @Modifying
    @Query(
        """
            UPDATE UserChannelEntity uc SET uc.lastReadMsgSeq = :lastReadMsgSeq
            WHERE uc.userId = :userId and uc.channelId = :channelId
        """
    )
    fun updateLastReadMsgSeqByUserIdAndChannelId(
        @Param("userId") userId: Long,
        @Param("channelId") channelId: Long,
        @Param("lastReadMsgSeq") lastReadMsgSeq: Long,
    ): Int

    fun deleteByUserIdAndChannelId(userId: Long, channelId: Long)
}