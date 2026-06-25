package com.streamvault.app.data.models

import com.google.gson.annotations.SerializedName

// ─── Auth / User Info ────────────────────────────────────────────────────────

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo? = null,
    @SerializedName("server_info") val serverInfo: ServerInfo? = null
)

data class UserInfo(
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("auth") val auth: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("exp_date") val expDate: String? = null,
    @SerializedName("is_trial") val isTrial: String = "0",
    @SerializedName("active_cons") val activeCons: String = "0",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("max_connections") val maxConnections: String = "1",
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String> = emptyList()
)

data class ServerInfo(
    @SerializedName("url") val url: String = "",
    @SerializedName("port") val port: String = "",
    @SerializedName("https_port") val httpsPort: String = "",
    @SerializedName("server_protocol") val serverProtocol: String = "http",
    @SerializedName("rtmp_port") val rtmpPort: String = "",
    @SerializedName("timezone") val timezone: String = "",
    @SerializedName("timestamp_now") val timestampNow: Long = 0L,
    @SerializedName("time_now") val timeNow: String = ""
)

// ─── Categories ──────────────────────────────────────────────────────────────

data class Category(
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_name") val categoryName: String = "",
    @SerializedName("parent_id") val parentId: Int = 0
)

// ─── Live Streams ─────────────────────────────────────────────────────────────

data class LiveStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "live",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("epg_channel_id") val epgChannelId: String? = null,
    @SerializedName("added") val added: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("custom_sid") val customSid: String = "",
    @SerializedName("tv_archive") val tvArchive: Int = 0,
    @SerializedName("direct_source") val directSource: String = "",
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int = 0
)

// ─── VOD Streams ─────────────────────────────────────────────────────────────

data class VodStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "movie",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("rating") val rating: String = "",
    @SerializedName("rating_5based") val rating5Based: Double = 0.0,
    @SerializedName("added") val added: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mkv",
    @SerializedName("custom_sid") val customSid: String = "",
    @SerializedName("direct_source") val directSource: String = ""
)

data class VodInfoResponse(
    @SerializedName("info") val info: VodInfo? = null,
    @SerializedName("movie_data") val movieData: VodMovieData? = null
)

data class VodInfo(
    @SerializedName("kinopoisk_url") val kinopoiskUrl: String = "",
    @SerializedName("tmdb_id") val tmdbId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("o_name") val originalName: String = "",
    @SerializedName("cover_big") val coverBig: String = "",
    @SerializedName("movie_image") val movieImage: String = "",
    @SerializedName("releasedate") val releaseDate: String = "",
    @SerializedName("episode_run_time") val episodeRunTime: String = "",
    @SerializedName("youtube_trailer") val youtubeTrailer: String = "",
    @SerializedName("director") val director: String = "",
    @SerializedName("actors") val actors: String = "",
    @SerializedName("cast") val cast: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("plot") val plot: String = "",
    @SerializedName("age") val age: String = "",
    @SerializedName("mpaa_rating") val mpaaRating: String = "",
    @SerializedName("rating_count_kinopoisk") val ratingCountKinopoisk: Int = 0,
    @SerializedName("country") val country: String = "",
    @SerializedName("genre") val genre: String = "",
    @SerializedName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerializedName("duration_secs") val durationSecs: Int = 0,
    @SerializedName("duration") val duration: String = "",
    @SerializedName("bitrate") val bitrate: Int = 0,
    @SerializedName("rating") val rating: String = ""
)

data class VodMovieData(
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("added") val added: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mkv",
    @SerializedName("custom_sid") val customSid: String = "",
    @SerializedName("direct_source") val directSource: String = ""
)

// ─── Series ──────────────────────────────────────────────────────────────────

data class SeriesStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("cover") val cover: String = "",
    @SerializedName("plot") val plot: String = "",
    @SerializedName("cast") val cast: String = "",
    @SerializedName("director") val director: String = "",
    @SerializedName("genre") val genre: String = "",
    @SerializedName("releaseDate") val releaseDate: String = "",
    @SerializedName("last_modified") val lastModified: String = "",
    @SerializedName("rating") val rating: String = "",
    @SerializedName("rating_5based") val rating5Based: Double = 0.0,
    @SerializedName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerializedName("youtube_trailer") val youtubeTrailer: String = "",
    @SerializedName("episode_run_time") val episodeRunTime: String = "",
    @SerializedName("category_id") val categoryId: String = ""
)

data class SeriesInfoResponse(
    @SerializedName("info") val info: SeriesInfo? = null,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>? = null,
    @SerializedName("seasons") val seasons: List<Season>? = null
)

data class SeriesInfo(
    @SerializedName("name") val name: String = "",
    @SerializedName("cover") val cover: String = "",
    @SerializedName("plot") val plot: String = "",
    @SerializedName("cast") val cast: String = "",
    @SerializedName("director") val director: String = "",
    @SerializedName("genre") val genre: String = "",
    @SerializedName("releaseDate") val releaseDate: String = "",
    @SerializedName("last_modified") val lastModified: String = "",
    @SerializedName("rating") val rating: String = "",
    @SerializedName("rating_5based") val rating5Based: Double = 0.0,
    @SerializedName("backdrop_path") val backdropPath: List<String> = emptyList(),
    @SerializedName("youtube_trailer") val youtubeTrailer: String = "",
    @SerializedName("episode_run_time") val episodeRunTime: String = "",
    @SerializedName("category_id") val categoryId: String = ""
)

data class Season(
    @SerializedName("air_date") val airDate: String = "",
    @SerializedName("episode_count") val episodeCount: Int = 0,
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("cover") val cover: String = "",
    @SerializedName("cover_big") val coverBig: String = ""
)

data class Episode(
    @SerializedName("id") val id: String = "",
    @SerializedName("episode_num") val episodeNum: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mkv",
    @SerializedName("info") val info: EpisodeInfo? = null,
    @SerializedName("custom_sid") val customSid: String = "",
    @SerializedName("added") val added: String = "",
    @SerializedName("season") val season: Int = 0,
    @SerializedName("direct_source") val directSource: String = ""
)

data class EpisodeInfo(
    @SerializedName("tmdb_id") val tmdbId: Int = 0,
    @SerializedName("releasedate") val releaseDate: String = "",
    @SerializedName("plot") val plot: String = "",
    @SerializedName("duration_secs") val durationSecs: Int = 0,
    @SerializedName("duration") val duration: String = "",
    @SerializedName("movie_image") val movieImage: String = "",
    @SerializedName("bitrate") val bitrate: Int = 0,
    @SerializedName("rating") val rating: String = ""
)

// ─── EPG ─────────────────────────────────────────────────────────────────────

data class EpgResponse(
    @SerializedName("epg_listings") val epgListings: List<EpgProgram> = emptyList()
)

data class EpgProgram(
    @SerializedName("id") val id: String = "",
    @SerializedName("epg_id") val epgId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("lang") val lang: String = "",
    @SerializedName("start") val start: String = "",
    @SerializedName("end") val end: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("channel_id") val channelId: String = "",
    @SerializedName("start_timestamp") val startTimestamp: Long = 0L,
    @SerializedName("stop_timestamp") val stopTimestamp: Long = 0L
) {
    val decodedTitle: String get() = try {
        String(android.util.Base64.decode(title, android.util.Base64.DEFAULT))
    } catch (e: Exception) { title }

    val decodedDescription: String get() = try {
        String(android.util.Base64.decode(description, android.util.Base64.DEFAULT))
    } catch (e: Exception) { description }
}

// ─── UI Domain Models ─────────────────────────────────────────────────────────

enum class ContentType { LIVE, VOD, SERIES, EPISODE }

data class MediaItem(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val categoryId: String = "",
    val contentType: ContentType,
    val streamUrl: String = "",
    val containerExtension: String = "",
    val rating: Double = 0.0,
    val description: String = "",
    val director: String = "",
    val cast: String = "",
    val releaseDate: String = "",
    val genre: String = "",
    val duration: String = "",
    val isFavorite: Boolean = false,
    val watchProgress: Long = 0L,
    val totalDuration: Long = 0L
)
