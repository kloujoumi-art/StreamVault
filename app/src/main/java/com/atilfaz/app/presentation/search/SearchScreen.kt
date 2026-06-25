package com.atilfaz.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    onNavigateToSeriesDetail: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search everything...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AtilfazAccentCyan,
                            unfocusedContainerColor = AtilfazCardDark,
                            focusedContainerColor = AtilfazCardDark,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AtilfazAccentCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            if (!state.hasSearched) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Search for content", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.4f))
                    }
                }
                return@Scaffold
            }

            val hasResults = state.liveResults.isNotEmpty() ||
                    state.vodResults.isNotEmpty() ||
                    state.seriesResults.isNotEmpty()

            if (!hasResults) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.4f))
                        Text("Try a different search term", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.3f))
                    }
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val showLive = state.selectedFilter == SearchFilter.ALL || state.selectedFilter == SearchFilter.LIVE
                val showVod = state.selectedFilter == SearchFilter.ALL || state.selectedFilter == SearchFilter.VOD
                val showSeries = state.selectedFilter == SearchFilter.ALL || state.selectedFilter == SearchFilter.SERIES

                if (showLive && state.liveResults.isNotEmpty()) {
                    item {
                        SearchSectionHeader("Live TV (${state.liveResults.size})", Icons.Default.Tv)
                    }
                    items(state.liveResults.take(20)) { stream ->
                        SearchResultItem(
                            title = stream.name,
                            imageUrl = stream.streamIcon,
                            subtitle = "Live Channel",
                            icon = Icons.Default.Tv,
                            badgeText = "LIVE",
                            badgeColor = LiveBadgeColor,
                            onClick = {
                                scope.launch {
                                    val url = viewModel.buildLiveUrl(stream.streamId)
                                    onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_LIVE, stream.streamId)
                                }
                            }
                        )
                    }
                }

                if (showVod && state.vodResults.isNotEmpty()) {
                    item { SearchSectionHeader("Movies (${state.vodResults.size})", Icons.Default.Movie) }
                    items(state.vodResults.take(20)) { stream ->
                        SearchResultItem(
                            title = stream.name,
                            imageUrl = stream.streamIcon,
                            subtitle = "Movie",
                            icon = Icons.Default.Movie,
                            rating = stream.rating5Based,
                            onClick = {
                                scope.launch {
                                    val url = viewModel.buildVodUrl(stream.streamId, stream.containerExtension)
                                    onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_VOD, stream.streamId)
                                }
                            }
                        )
                    }
                }

                if (showSeries && state.seriesResults.isNotEmpty()) {
                    item { SearchSectionHeader("Series (${state.seriesResults.size})", Icons.Default.VideoLibrary) }
                    items(state.seriesResults.take(20)) { series ->
                        SearchResultItem(
                            title = series.name,
                            imageUrl = series.cover,
                            subtitle = series.genre.ifEmpty { "Series" }.take(30),
                            icon = Icons.Default.VideoLibrary,
                            rating = series.rating5Based,
                            onClick = { onNavigateToSeriesDetail(series.seriesId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = AtilfazAccentCyan, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}

@Composable
private fun SearchResultItem(
    title: String,
    imageUrl: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String? = null,
    badgeColor: Color = Color.Transparent,
    rating: Double = 0.0,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCardDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AtilfazDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(icon, null, tint = AtilfazAccentCyan.copy(0.5f), modifier = Modifier.size(28.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.6f),
                        maxLines = 1
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(badgeText, style = MaterialTheme.typography.labelSmall, color = badgeColor)
                        }
                    }
                    if (rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = AtilfazGold, modifier = Modifier.size(12.dp))
                            Text(String.format("%.1f", rating), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                        }
                    }
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color.White.copy(0.6f))
        }
    }
}
