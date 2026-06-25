package com.atilfaz.app.presentation.livetv

import androidx.compose.foundation.background
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
fun LiveTvScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    onNavigateToEpg: (Int) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live TV", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AtilfazBlack)
            )
        },
        containerColor = AtilfazBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search channels...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AtilfazAccentCyan,
                    unfocusedContainerColor = AtilfazCardDark,
                    focusedContainerColor = AtilfazCardDark
                )
            )

            // Category Chips
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == "all",
                            onClick = { viewModel.selectCategory("all") },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AtilfazAccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.categoryId,
                            onClick = { viewModel.selectCategory(category.categoryId) },
                            label = { Text(category.categoryName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AtilfazAccentCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AtilfazAccentCyan)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredStreams) { stream ->
                        LiveChannelListItem(
                            name = stream.name,
                            iconUrl = stream.streamIcon,
                            hasEpg = !stream.epgChannelId.isNullOrEmpty(),
                            hasArchive = stream.tvArchive == 1,
                            onClick = {
                                scope.launch {
                                    val url = viewModel.buildStreamUrl(stream.streamId)
                                    onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_LIVE, stream.streamId)
                                }
                            },
                            onEpgClick = { onNavigateToEpg(stream.streamId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveChannelListItem(
    name: String,
    iconUrl: String,
    hasEpg: Boolean,
    hasArchive: Boolean,
    onClick: () -> Unit,
    onEpgClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                AsyncImage(
                    model = iconUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LiveBadgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE", style = MaterialTheme.typography.labelSmall, color = LiveBadgeColor)
                    }
                    if (hasArchive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AtilfazAccentPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("REPLAY", style = MaterialTheme.typography.labelSmall, color = AtilfazAccentPurple)
                        }
                    }
                }
            }
            if (hasEpg) {
                IconButton(onClick = onEpgClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "EPG",
                        tint = AtilfazAccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color.White.copy(alpha = 0.6f))
        }
    }
}
