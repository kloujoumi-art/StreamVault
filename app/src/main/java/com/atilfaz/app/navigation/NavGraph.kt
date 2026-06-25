package com.atilfaz.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atilfaz.app.presentation.auth.LoginScreen
import com.atilfaz.app.presentation.main.MainScreen
import com.atilfaz.app.presentation.player.PlayerScreen
import com.atilfaz.app.presentation.series.SeriesDetailScreen
import com.atilfaz.app.presentation.search.SearchScreen
import com.atilfaz.app.presentation.epg.EpgScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition  = { fadeIn(animationSpec = tween(250)) },
        exitTransition   = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition  = { fadeIn(animationSpec = tween(250)) },
        popExitTransition   = { fadeOut(animationSpec = tween(250)) }
    ) {
        // ── Login ─────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main (bottom nav: Live TV / Movies / Series / Settings) ───────
        composable(Screen.Home.route) {
            MainScreen(
                onNavigateToPlayer = { url, title, type, id ->
                    navController.navigate(Screen.Player.createRoute(url, title, type, id))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Player ────────────────────────────────────────────────────────
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("streamUrl")   { type = NavType.StringType },
                navArgument("streamTitle") { type = NavType.StringType },
                navArgument("streamType")  { type = NavType.StringType },
                navArgument("streamId")    { type = NavType.IntType }
            ),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) { backStack ->
            val url   = java.net.URLDecoder.decode(backStack.arguments?.getString("streamUrl") ?: "", "UTF-8")
            val title = backStack.arguments?.getString("streamTitle") ?: ""
            val type  = backStack.arguments?.getString("streamType") ?: "live"
            val id    = backStack.arguments?.getInt("streamId") ?: 0
            PlayerScreen(
                streamUrl    = url,
                streamTitle  = title,
                streamType   = type,
                streamId     = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEpg = { streamId ->
                    navController.navigate(Screen.Epg.createRoute(streamId))
                }
            )
        }

        // ── Series Detail ─────────────────────────────────────────────────
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

        // ── Search ────────────────────────────────────────────────────────
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

        // ── EPG ───────────────────────────────────────────────────────────
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
