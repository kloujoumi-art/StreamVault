package com.streamvault.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object LiveTv : Screen("live_tv")
    object Vod : Screen("vod")
    object Series : Screen("series")
    object Favorites : Screen("favorites")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Epg : Screen("epg/{streamId}") {
        fun createRoute(streamId: Int) = "epg/$streamId"
    }
    object Player : Screen(
        "player/{streamUrl}/{streamTitle}/{streamType}/{streamId}"
    ) {
        fun createRoute(
            streamUrl: String,
            streamTitle: String,
            streamType: String,
            streamId: Int
        ) = "player/${encode(streamUrl)}/$streamTitle/$streamType/$streamId"

        private fun encode(url: String) =
            java.net.URLEncoder.encode(url, "UTF-8")
    }
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: Int) = "series_detail/$seriesId"
    }
    object VodDetail : Screen("vod_detail/{vodId}") {
        fun createRoute(vodId: Int) = "vod_detail/$vodId"
    }
}
