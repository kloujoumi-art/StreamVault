package com.atilfaz.app.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.atilfaz.app.presentation.livetv.LiveTvScreen
import com.atilfaz.app.presentation.vod.VodScreen
import com.atilfaz.app.presentation.series.SeriesScreen
import com.atilfaz.app.presentation.settings.SettingsScreen
import com.atilfaz.app.ui.theme.*

sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object LiveTv   : BottomTab("tab_live",     "LIVE TV",  Icons.Default.Tv)
    object Movies   : BottomTab("tab_movies",   "MOVIES",   Icons.Default.Movie)
    object Series   : BottomTab("tab_series",   "SERIES",   Icons.Default.VideoLibrary)
    object Settings : BottomTab("tab_settings", "SETTINGS", Icons.Default.Settings)
}

val bottomTabs = listOf(
    BottomTab.LiveTv,
    BottomTab.Movies,
    BottomTab.Series,
    BottomTab.Settings
)

@Composable
fun MainScreen(
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    onLogout: () -> Unit
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0A0A),
                tonalElevation = 0.dp,
                modifier = Modifier.height(62.dp)
            ) {
                bottomTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AtilfazBlueLight,
                            selectedTextColor = AtilfazBlueLight,
                            unselectedIconColor = Color(0xFF707070),
                            unselectedTextColor = Color(0xFF707070),
                            indicatorColor = AtilfazBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = AtilfazBackground
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.LiveTv.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.LiveTv.route) {
                LiveTvScreen(onNavigateToPlayer = onNavigateToPlayer)
            }
            composable(BottomTab.Movies.route) {
                VodScreen(onNavigateToPlayer = onNavigateToPlayer)
            }
            composable(BottomTab.Series.route) {
                SeriesScreen(
                    onNavigateToPlayer = onNavigateToPlayer,
                    onNavigateToSeriesDetail = {}
                )
            }
            composable(BottomTab.Settings.route) {
                SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
