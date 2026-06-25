package com.streamvault.app.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.streamvault.app.data.local.entities.FavoriteEntity
import com.streamvault.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val tabs = listOf("All", "Live TV", "Movies", "Series")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (state.allFavorites.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearAllFavorites) {
                            Icon(Icons.Default.DeleteSweep, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StreamVaultBlack)
            )
        },
        containerColor = StreamVaultBlack
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = StreamVaultDarkSurface,
                contentColor = StreamVaultAccentCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = StreamVaultAccentCyan
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                title,
                                color = if (state.selectedTab == index) StreamVaultAccentCyan else Color.White.copy(0.6f)
                            )
                        }
                    )
                }
            }

            val currentList = when (state.selectedTab) {
                1 -> state.liveFavorites
                2 -> state.vodFavorites
                3 -> state.seriesFavorites
                else -> state.allFavorites
            }

            if (currentList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No favorites yet", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.5f))
                        Text("Add channels to your favorites", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.3f))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList) { fav ->
                        FavoriteItem(
                            favorite = fav,
                            onPlay = {
                                scope.launch {
                                    val url = viewModel.buildStreamUrl(fav)
                                    onNavigateToPlayer(url, fav.title, fav.streamType, fav.streamId)
                                }
                            },
                            onRemove = { viewModel.removeFavorite(fav.streamId, fav.streamType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    favorite: FavoriteEntity,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StreamVaultCardDark)
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
                    .background(StreamVaultDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (favorite.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = favorite.thumbnailUrl,
                        contentDescription = favorite.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val icon = when (favorite.streamType) {
                        "live" -> Icons.Default.Tv
                        "movie" -> Icons.Default.Movie
                        else -> Icons.Default.VideoLibrary
                    }
                    Icon(icon, null, tint = StreamVaultAccentCyan, modifier = Modifier.size(28.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val typeLabel = when (favorite.streamType) {
                    "live" -> "Live TV"
                    "movie" -> "Movie"
                    else -> "Series"
                }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = StreamVaultAccentCyan
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.HeartBroken, null, tint = Color.Red.copy(0.8f), modifier = Modifier.size(20.dp))
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color.White.copy(0.6f))
        }
    }
}
