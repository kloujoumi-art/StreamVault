package com.streamvault.app.data.repository

import com.streamvault.app.data.api.ApiResult
import com.streamvault.app.data.api.XtreamApiService
import com.streamvault.app.data.api.safeApiCall
import com.streamvault.app.data.local.dao.EpgDao
import com.streamvault.app.data.local.entities.EpgProgramEntity
import com.streamvault.app.data.models.EpgProgram
import com.streamvault.app.data.preferences.UserPreferences
import com.streamvault.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpgRepository @Inject constructor(
    private val apiService: XtreamApiService,
    private val epgDao: EpgDao,
    private val userPreferences: UserPreferences
) {
    fun getEpgForStream(streamId: Int): Flow<List<EpgProgramEntity>> =
        epgDao.getEpgForStream(streamId)

    suspend fun fetchAndCacheEpg(streamId: Int, forceRefresh: Boolean = false): ApiResult<List<EpgProgram>> {
        val cacheExpiryMs = TimeUnit.HOURS.toMillis(Constants.EPG_CACHE_HOURS.toLong())
        val lastCache = epgDao.getLastCacheTime(streamId) ?: 0L
        val isCacheValid = (System.currentTimeMillis() - lastCache) < cacheExpiryMs

        if (isCacheValid && !forceRefresh) {
            val cached = epgDao.getEpgForStreamOnce(streamId)
            return ApiResult.Success(cached.map { it.toEpgProgram() })
        }

        val username = userPreferences.getUsernameOnce()
        val password = userPreferences.getPasswordOnce()

        return safeApiCall {
            val response = apiService.getShortEpg(username, password, streamId = streamId, limit = 12)
            val programs = response.body()?.epgListings ?: emptyList()
            epgDao.clearForStream(streamId)
            epgDao.insertAll(programs.map { it.toEntity(streamId) })
            programs
        }
    }

    suspend fun clearExpiredCache() {
        val cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(Constants.EPG_CACHE_HOURS.toLong())
        epgDao.clearExpired(cutoff)
    }

    private fun EpgProgramEntity.toEpgProgram() = EpgProgram(
        id = id,
        title = title,
        description = description,
        startTimestamp = startTimestamp,
        stopTimestamp = stopTimestamp,
        channelId = channelId
    )

    private fun EpgProgram.toEntity(streamId: Int) = EpgProgramEntity(
        id = "${streamId}_${startTimestamp}",
        streamId = streamId,
        title = decodedTitle,
        description = decodedDescription,
        startTimestamp = startTimestamp,
        stopTimestamp = stopTimestamp,
        channelId = channelId
    )
}
