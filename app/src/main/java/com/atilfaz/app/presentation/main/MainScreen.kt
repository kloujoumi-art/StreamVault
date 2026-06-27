package com.atilfaz.app.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
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
import com.atilfaz.app.utils.handleDpadAction
import com.atilfaz.app.utils.rememberIsTV

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
    val isTV = rememberIsTV()

    val content = @Composable {
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.LiveTv.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                BottomTab.LiveTv.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { LiveTvScreen(onNavigateToPlayer = onNavigateToPlayer) }
            composable(
                BottomTab.Movies.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { VodScreen(onNavigateToPlayer = onNavigateToPlayer) }
            composable(
                BottomTab.Series.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { SeriesScreen(onNavigateToSeriesDetail = {}) }
            composable(
                BottomTab.Settings.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { SettingsScreen(onLogout = onLogout) }
        }
    }

    val onTabSelect: (BottomTab) -> Unit = { tab ->
        tabNavController.navigate(tab.route) {
            popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (isTV) {
        // ── Android TV : rail vertical gauche ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(AtilfazBackground)
        ) {
            TvNavigationRail(
                tabs = bottomTabs,
                currentRoute = currentRoute,
                onTabSelected = onTabSelect
            )
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AtilfazBorder))
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { content() }
        }
    } else {
        // ── Téléphone / Tablette : barre de nav flottante en bas ──────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AtilfazBackground)
        ) {
            NavHost(
                navController = tabNavController,
                startDestination = BottomTab.LiveTv.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp)
            ) {
                composable(BottomTab.LiveTv.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
                    LiveTvScreen(onNavigateToPlayer = onNavigateToPlayer)
                }
                composable(BottomTab.Movies.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
                    VodScreen(onNavigateToPlayer = onNavigateToPlayer)
                }
                composable(BottomTab.Series.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
                    SeriesScreen(onNavigateToSeriesDetail = {})
                }
                composable(BottomTab.Settings.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
                    SettingsScreen(onLogout = onLogout)
                }
            }

            GlassBottomNav(
                tabs = bottomTabs,
                currentRoute = currentRoute,
                onTabSelected = onTabSelect,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ── TV : Rail vertical gauche ──────────────────────────────────────────────────

@Composable
private fun TvNavigationRail(
    tabs: List<BottomTab>,
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .fillMaxHeight()
            .background(Color(0xFF080808))
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AtilfazBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(32.dp))

        tabs.forEach { tab ->
            TvRailItem(
                tab = tab,
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TvRailItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val highlight = selected || isFocused
    val bgColor = if (selected) AtilfazBlue.copy(alpha = 0.25f)
                  else if (isFocused) AtilfazBlue.copy(alpha = 0.15f)
                  else Color.Transparent
    val iconColor = if (highlight) AtilfazBlueLight else Color(0xFF555555)
    val borderColor = if (isFocused) AtilfazBlueLight else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .focusable()
            .handleDpadAction(onClick)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tab.label,
                color = iconColor,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }
    }
}

// ── Téléphone : barre de navigation flottante ─────────────────────────────────

@Composable
private fun GlassBottomNav(
    tabs: List<BottomTab>,
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xFF000000))
                    )
                )
        )

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
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A1A1A), Color(0xFF0F0F0F))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2A2A2A), AtilfazBlue.copy(alpha = 0.4f), Color(0xFF2A2A2A))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    PhoneNavItem(
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
private fun PhoneNavItem(
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
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                color = iconColor,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                letterSpacing = 0.5.sp
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .width(16.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Brush.horizontalGradient(colors = listOf(AtilfazBlue, AtilfazBlueLight)))
            )
        }
    }
}

