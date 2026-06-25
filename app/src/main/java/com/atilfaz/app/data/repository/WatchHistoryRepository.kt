package com.atilfaz.app.data.repository

import com.atilfaz.app.data.local.dao.WatchHistoryDao
import com.atilfaz.app.data.local.entities.WatchHistoryEntity
import com.atilfaz.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepository @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) {
    fun getRecentHistory(limit: Int = 20): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.getRecentHistory(limit)

    suspend fun saveProgress(
        streamId: Int,
        title: String,
        thumbnailUrl: String,
        streamType: String,
        streamUrl: String,
        progressMs: Long,
        durationMs: Long
    ) {
        val count = watchHistoryDao.getCount()
        if (count >= Constants.MAX_HISTORY_ITEMS) {
            watchHistoryDao.trimOldest(count - Constants.MAX_HISTORY_ITEMS + 1)
        }
        watchHistoryDao.upsertProgress(
            streamId = streamId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            streamType = streamType,
            streamUrl = streamUrl,
            watchedAt = System.currentTimeMillis(),
            progressMs = progressMs,
            durationMs = durationMs
        )
    }

    suspend fun getProgress(streamId: Int, type: String): WatchHistoryEntity? =
        watchHistoryDao.getProgress(streamId, type)

    suspend fun removeEntry(streamId: Int, type: String) =
        watchHistoryDao.deleteEntry(streamId, type)

    suspend fun clearAll() = watchHistoryDao.clearAll()
}
