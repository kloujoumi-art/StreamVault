package com.atilfaz.app.data.repository

import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.api.XtreamApiService
import com.atilfaz.app.data.api.safeApiCall
import com.atilfaz.app.data.models.*
import com.atilfaz.app.data.preferences.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val apiService: XtreamApiService,
    private val userPreferences: UserPreferences
) {
    private suspend fun getCredentials(): Triple<String, String, String> {
        return Triple(
            userPreferences.getServerUrlOnce(),
            userPreferences.getUsernameOnce(),
            userPreferences.getPasswordOnce()
        )
    }

    // ── Live TV ───────────────────────────────────────────────────────────────

    suspend fun getLiveCategories(): ApiResult<List<Category>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getLiveCategories(username, password).body()
                ?: throw Exception("Failed to fetch live categories")
        }
    }

    suspend fun getLiveStreams(categoryId: String? = null): ApiResult<List<LiveStream>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getLiveStreams(username, password, categoryId = categoryId).body()
                ?: throw Exception("Failed to fetch live streams")
        }
    }

    // ── VOD ───────────────────────────────────────────────────────────────────

    suspend fun getVodCategories(): ApiResult<List<Category>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getVodCategories(username, password).body()
                ?: throw Exception("Failed to fetch VOD categories")
        }
    }

    suspend fun getVodStreams(categoryId: String? = null): ApiResult<List<VodStream>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getVodStreams(username, password, categoryId = categoryId).body()
                ?: throw Exception("Failed to fetch VOD streams")
        }
    }

    suspend fun getVodInfo(vodId: Int): ApiResult<VodInfoResponse> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getVodInfo(username, password, vodId = vodId).body()
                ?: throw Exception("Failed to fetch VOD info")
        }
    }

    // ── Series ────────────────────────────────────────────────────────────────

    suspend fun getSeriesCategories(): ApiResult<List<Category>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getSeriesCategories(username, password).body()
                ?: throw Exception("Failed to fetch series categories")
        }
    }

    suspend fun getSeries(categoryId: String? = null): ApiResult<List<SeriesStream>> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getSeries(username, password, categoryId = categoryId).body()
                ?: throw Exception("Failed to fetch series")
        }
    }

    suspend fun getSeriesInfo(seriesId: Int): ApiResult<SeriesInfoResponse> {
        val (_, username, password) = getCredentials()
        return safeApiCall {
            apiService.getSeriesInfo(username, password, seriesId = seriesId).body()
                ?: throw Exception("Failed to fetch series info")
        }
    }

    // ── Stream URL Builder ────────────────────────────────────────────────────

    suspend fun buildLiveStreamUrl(streamId: Int, ext: String = "ts"): String {
        val (serverUrl, username, password) = getCredentials()
        val base = serverUrl.trimEnd('/')
        return "$base/live/$username/$password/$streamId.$ext"
    }

    suspend fun buildVodStreamUrl(streamId: Int, ext: String = "mkv"): String {
        val (serverUrl, username, password) = getCredentials()
        val base = serverUrl.trimEnd('/')
        return "$base/movie/$username/$password/$streamId.$ext"
    }

    suspend fun buildSeriesEpisodeUrl(episodeId: String, ext: String = "mkv"): String {
        val (serverUrl, username, password) = getCredentials()
        val base = serverUrl.trimEnd('/')
        return "$base/series/$username/$password/$episodeId.$ext"
    }
}
