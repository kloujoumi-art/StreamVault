package com.atilfaz.app.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    object LiveTv   : BottomTab("tab_live",     "LIVE",     Icons.Default.Tv)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtilfazBackground)
    ) {
        // Content fills entire screen including behind nav bar
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.LiveTv.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp)
        ) {
            composable(
                BottomTab.LiveTv.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                LiveTvScreen(onNavigateToPlayer = onNavigateToPlayer)
            }
            composable(
                BottomTab.Movies.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                VodScreen(onNavigateToPlayer = onNavigateToPlayer)
            }
            composable(
                BottomTab.Series.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                SeriesScreen(onNavigateToSeriesDetail = {})
            }
            composable(
                BottomTab.Settings.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                SettingsScreen(onLogout = onLogout)
            }
        }

        // Floating glass bottom navigation bar
        GlassBottomNav(
            tabs = bottomTabs,
            currentRoute = currentRoute,
            onTabSelected = { tab ->
                tabNavController.navigate(tab.route) {
                    popUpTo(tabNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun GlassBottomNav(
    tabs: List<BottomTab>,
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Gradient fade at bottom so content blends into nav
    Box(modifier = modifier.fillMaxWidth()) {
        // Dark gradient overlay from transparent → black (content fades into nav bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xCC000000),
                            Color(0xFF000000)
                        )
                    )
                )
        )

        // Nav bar pill — floating glass style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1A1A),
                                Color(0xFF0F0F0F)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2A2A2A),
                                AtilfazBlue.copy(alpha = 0.4f),
                                Color(0xFF2A2A2A)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) AtilfazBlueLight else Color(0xFF555555),
        animationSpec = tween(250),
        label = "navIconColor"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(250),
        label = "navBgAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selected blue pill indicator
        if (selected) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AtilfazBlue.copy(alpha = 0.18f * bgAlpha))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                color = iconColor,
                fontSize = 8.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                letterSpacing = 0.5.sp
            )
        }

        // Active dot indicator at bottom of pill
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .width(16.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(AtilfazBlue, AtilfazBlueLight)
                        )
                    )
            )
        }
    }
}
