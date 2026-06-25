package com.streamvault.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.streamvault.app.presentation.auth.LoginScreen
import com.streamvault.app.presentation.home.HomeScreen
import com.streamvault.app.presentation.livetv.LiveTvScreen
import com.streamvault.app.presentation.vod.VodScreen
import com.streamvault.app.presentation.series.SeriesScreen
import com.streamvault.app.presentation.series.SeriesDetailScreen
import com.streamvault.app.presentation.player.PlayerScreen
import com.streamvault.app.presentation.favorites.FavoritesScreen
import com.streamvault.app.presentation.search.SearchScreen
import com.streamvault.app.presentation.settings.SettingsScreen
import com.streamvault.app.presentation.epg.EpgScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { it / 4 },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { it / 4 },
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLiveTV = { navController.navigate(Screen.LiveTv.route) },
                onNavigateToVod = { navController.navigate(Screen.Vod.route) },
                onNavigateToSeries = { navController.navigate(Screen.Series.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                }
            )
        }

        composable(Screen.LiveTv.route) {
            LiveTvScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                },
                onNavigateToEpg = { streamId ->
                    navController.navigate(Screen.Epg.createRoute(streamId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Vod.route) {
            VodScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Series.route) {
            SeriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSeriesDetail = { seriesId ->
                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(
            route = Screen.SeriesDetail.route,
            arguments = listOf(navArgument("seriesId") { type = NavType.IntType })
        ) { backStack ->
            val seriesId = backStack.arguments?.getInt("seriesId") ?: return@composable
            SeriesDetailScreen(
                seriesId = seriesId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("streamTitle") { type = NavType.StringType },
                navArgument("streamType") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType }
            ),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) { backStack ->
            val url = java.net.URLDecoder.decode(
                backStack.arguments?.getString("streamUrl") ?: "", "UTF-8"
            )
            val title = backStack.arguments?.getString("streamTitle") ?: ""
            val type = backStack.arguments?.getString("streamType") ?: "live"
            val id = backStack.arguments?.getInt("streamId") ?: 0
            PlayerScreen(
                streamUrl = url,
                streamTitle = title,
                streamType = type,
                streamId = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEpg = { streamId ->
                    navController.navigate(Screen.Epg.createRoute(streamId))
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                },
                onNavigateToSeriesDetail = { seriesId ->
                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Epg.route,
            arguments = listOf(navArgument("streamId") { type = NavType.IntType })
        ) { backStack ->
            val streamId = backStack.arguments?.getInt("streamId") ?: return@composable
            EpgScreen(
                streamId = streamId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
