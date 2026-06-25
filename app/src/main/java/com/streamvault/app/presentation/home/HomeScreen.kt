package com.streamvault.app.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.streamvault.app.data.models.LiveStream
import com.streamvault.app.data.models.SeriesStream
import com.streamvault.app.data.models.VodStream
import com.streamvault.app.ui.theme.*
import com.streamvault.app.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLiveTV: () -> Unit,
    onNavigateToVod: () -> Unit,
    onNavigateToSeries: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = StreamVaultAccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = Constants.APP_NAME,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StreamVaultBlack
                )
            )
        },
        containerColor = StreamVaultBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Quick Access Section
            item {
                QuickAccessSection(
                    onNavigateToLiveTV = onNavigateToLiveTV,
                    onNavigateToVod = onNavigateToVod,
                    onNavigateToSeries = onNavigateToSeries,
                    onNavigateToFavorites = onNavigateToFavorites
                )
            }

            // Continue Watching
            if (state.continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(title = "Continue Watching", icon = Icons.Default.History)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.continueWatching) { item ->
                            ContinueWatchingCard(
                                title = item.title,
                                thumbnailUrl = item.thumbnailUrl,
                                progressMs = item.progressMs,
                                durationMs = item.durationMs,
                                onClick = {
                                    onNavigateToPlayer(item.streamUrl, item.title, item.streamType, item.streamId)
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Live TV
            if (state.featuredLive.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Live TV",
                        icon = Icons.Default.Tv,
                        onSeeAll = onNavigateToLiveTV
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.featuredLive) { stream ->
                            LiveChannelCard(
                                stream = stream,
                                onClick = {
                                    onNavigateToPlayer(
                                        "",
                                        stream.name,
                                        Constants.STREAM_TYPE_LIVE,
                                        stream.streamId
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // VOD
            if (state.recentVod.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Movies",
                        icon = Icons.Default.Movie,
                        onSeeAll = onNavigateToVod
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recentVod) { stream ->
                            VodCard(
                                stream = stream,
                                onClick = {
                                    onNavigateToPlayer(
                                        "",
                                        stream.name,
                                        Constants.STREAM_TYPE_VOD,
                                        stream.streamId
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Series
            if (state.popularSeries.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Series",
                        icon = Icons.Default.VideoLibrary,
                        onSeeAll = onNavigateToSeries
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.popularSeries) { series ->
                            SeriesCard(series = series, onClick = {})
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Loading state
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StreamVaultAccentCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessSection(
    onNavigateToLiveTV: () -> Unit,
    onNavigateToVod: () -> Unit,
    onNavigateToSeries: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAccessButton(
            icon = Icons.Default.Tv,
            label = "Live TV",
            color = Color(0xFF0A84FF),
            onClick = onNavigateToLiveTV,
            modifier = Modifier.weight(1f)
        )
        QuickAccessButton(
            icon = Icons.Default.Movie,
            label = "Movies",
            color = Color(0xFF30D158),
            onClick = onNavigateToVod,
            modifier = Modifier.weight(1f)
        )
        QuickAccessButton(
            icon = Icons.Default.VideoLibrary,
            label = "Series",
            color = Color(0xFFFF9F0A),
            onClick = onNavigateToSeries,
            modifier = Modifier.weight(1f)
        )
        QuickAccessButton(
            icon = Icons.Default.Favorite,
            label = "Favorites",
            color = Color(0xFFFF375F),
            onClick = onNavigateToFavorites,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickAccessButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = StreamVaultAccentCyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("See All", color = StreamVaultAccentCyan, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    title: String,
    thumbnailUrl: String,
    progressMs: Long,
    durationMs: Long,
    onClick: () -> Unit
) {
    val progress = if (durationMs > 0) progressMs.toFloat() / durationMs.toFloat() else 0f
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp).align(Alignment.Center)
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = StreamVaultAccentCyan,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun LiveChannelCard(stream: LiveStream, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(StreamVaultDarkSurface)) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = stream.name,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(LiveBadgeColor)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
            Text(
                text = stream.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun VodCard(stream: VodStream, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = stream.streamIcon,
                    contentDescription = stream.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.7f)),
                                startY = 80f
                            )
                        )
                )
                if (stream.rating5Based > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = StreamVaultGold, modifier = Modifier.size(12.dp))
                        Text(
                            text = String.format("%.1f", stream.rating5Based),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            Text(
                text = stream.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun SeriesCard(series: SeriesStream, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = series.cover,
                    contentDescription = series.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = series.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
