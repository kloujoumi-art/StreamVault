package com.atilfaz.app.presentation.series

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.atilfaz.app.data.models.Episode
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(seriesId) { viewModel.loadSeriesDetail(seriesId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.seriesInfo?.info?.name ?: "Series", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AtilfazBlack)
            )
        },
        containerColor = AtilfazBlack
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AtilfazAccentCyan)
            }
            return@Scaffold
        }

        val info = state.seriesInfo ?: return@Scaffold
        val episodes = info.episodes ?: emptyMap()
        val seasons = info.seasons ?: emptyList()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Section
            item {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    AsyncImage(
                        model = info.info?.cover,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, AtilfazBlack),
                                startY = 100f
                            )
                        )
                    )
                }
            }

            // Info Section
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = info.info?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if ((info.info?.rating5Based ?: 0.0) > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = AtilfazGold, modifier = Modifier.size(16.dp))
                                Text(String.format("%.1f", info.info?.rating5Based), style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                        if (!info.info?.genre.isNullOrEmpty()) {
                            Text(info.info?.genre ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
                        }
                        if (!info.info?.releaseDate.isNullOrEmpty()) {
                            Text(info.info?.releaseDate?.take(4) ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
                        }
                    }
                    if (!info.info?.plot.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = info.info?.plot ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.8f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Season Selector
            if (seasons.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(seasons) { season ->
                            FilterChip(
                                selected = state.selectedSeason == season.seasonNumber,
                                onClick = { viewModel.selectSeason(season.seasonNumber) },
                                label = { Text("Season ${season.seasonNumber}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtilfazAccentCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Episodes
            val currentEpisodes = episodes[state.selectedSeason.toString()] ?: emptyList()
            items(currentEpisodes) { episode ->
                EpisodeItem(
                    episode = episode,
                    onClick = {
                        scope.launch {
                            val url = viewModel.buildEpisodeUrl(episode.id, episode.containerExtension)
                            onNavigateToPlayer(url, episode.title, Constants.STREAM_TYPE_EPISODE, episode.id.toIntOrNull() ?: 0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EpisodeItem(episode: Episode, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCardDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp, 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AtilfazDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (episode.info?.movieImage?.isNotEmpty() == true) {
                    AsyncImage(
                        model = episode.info.movieImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${episode.episodeNum}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AtilfazAccentCyan
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ep. ${episode.episodeNum} - ${episode.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!episode.info?.plot.isNullOrEmpty()) {
                    Text(
                        text = episode.info?.plot ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!episode.info?.duration.isNullOrEmpty()) {
                    Text(
                        text = episode.info?.duration ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtilfazAccentCyan
                    )
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color.White.copy(0.6f))
        }
    }
}
