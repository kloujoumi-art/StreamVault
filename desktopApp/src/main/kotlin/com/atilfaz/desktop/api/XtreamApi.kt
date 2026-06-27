package com.atilfaz.desktop.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class LiveCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class LiveStream(
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("epg_channel_id") val epgChannelId: String? = null,
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("tv_archive") val tvArchive: Int = 0
)

data class VodStream(
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("rating_5based") val rating: Double = 0.0,
    @SerializedName("container_extension") val containerExtension: String = "mkv"
)

data class SeriesStream(
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cover") val cover: String = "",
    @SerializedName("category_id") val categoryId: String = ""
)

data class XtreamCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
)

class XtreamApi(private val creds: XtreamCredentials) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val base get() = "${creds.serverUrl}/player_api.php?username=${creds.username}&password=${creds.password}"

    private fun get(url: String): String {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            return resp.body?.string() ?: throw Exception("Réponse vide")
        }
    }

    fun authenticate(): Boolean = try {
        val body = get("$base&action=get_live_categories")
        body.isNotEmpty() && !body.contains("\"user_info\":{\"status\":\"Disabled\"")
    } catch (_: Exception) { false }

    fun getLiveCategories(): List<LiveCategory> = try {
        val body = get("$base&action=get_live_categories")
        val type = object : TypeToken<List<LiveCategory>>() {}.type
        gson.fromJson(body, type)
    } catch (_: Exception) { emptyList() }

    fun getLiveStreams(categoryId: String? = null): List<LiveStream> = try {
        val url = if (categoryId != null) "$base&action=get_live_streams&category_id=$categoryId"
                  else "$base&action=get_live_streams"
        val body = get(url)
        val type = object : TypeToken<List<LiveStream>>() {}.type
        gson.fromJson(body, type)
    } catch (_: Exception) { emptyList() }

    fun getVodStreams(categoryId: String? = null): List<VodStream> = try {
        val url = if (categoryId != null) "$base&action=get_vod_streams&category_id=$categoryId"
                  else "$base&action=get_vod_streams"
        val body = get(url)
        val type = object : TypeToken<List<VodStream>>() {}.type
        gson.fromJson(body, type)
    } catch (_: Exception) { emptyList() }

    fun getSeries(categoryId: String? = null): List<SeriesStream> = try {
        val url = if (categoryId != null) "$base&action=get_series&category_id=$categoryId"
                  else "$base&action=get_series"
        val body = get(url)
        val type = object : TypeToken<List<SeriesStream>>() {}.type
        gson.fromJson(body, type)
    } catch (_: Exception) { emptyList() }

    fun buildLiveUrl(streamId: Int): String =
        "${creds.serverUrl}/live/${creds.username}/${creds.password}/$streamId.m3u8"

    fun buildVodUrl(streamId: Int, ext: String): String =
        "${creds.serverUrl}/movie/${creds.username}/${creds.password}/$streamId.$ext"
}
