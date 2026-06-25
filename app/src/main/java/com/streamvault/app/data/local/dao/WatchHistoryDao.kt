package com.streamvault.app.data.local.dao

import androidx.room.*
import com.streamvault.app.data.local.entities.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId AND streamType = :type LIMIT 1")
    suspend fun getProgress(streamId: Int, type: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity): Long

    @Query("""
        INSERT OR REPLACE INTO watch_history (streamId, title, thumbnailUrl, streamType, streamUrl, watchedAt, progressMs, durationMs)
        VALUES (:streamId, :title, :thumbnailUrl, :streamType, :streamUrl, :watchedAt, :progressMs, :durationMs)
    """)
    suspend fun upsertProgress(
        streamId: Int,
        title: String,
        thumbnailUrl: String,
        streamType: String,
        streamUrl: String,
        watchedAt: Long,
        progressMs: Long,
        durationMs: Long
    )

    @Query("DELETE FROM watch_history WHERE streamId = :streamId AND streamType = :type")
    suspend fun deleteEntry(streamId: Int, type: String)

    @Query("DELETE FROM watch_history WHERE uid IN (SELECT uid FROM watch_history ORDER BY watchedAt ASC LIMIT :count)")
    suspend fun trimOldest(count: Int)

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
