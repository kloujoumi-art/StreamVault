package com.atilfaz.app.presentation.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VodScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: VodViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        VodSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (state.sortBy == option) {
                                        Icon(Icons.Default.Check, null, tint = AtilfazAccentCyan)
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AtilfazBlack)
            )
        },
        containerColor = AtilfazBlack
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search movies...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AtilfazAccentCyan,
                    unfocusedContainerColor = AtilfazCardDark,
                    focusedContainerColor = AtilfazCardDark
                )
            )

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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredStreams) { stream ->
                        VodGridItem(
                            title = stream.name,
                            posterUrl = stream.streamIcon,
                            rating = stream.rating5Based,
                            onClick = {
                                scope.launch {
                                    val url = viewModel.buildStreamUrl(stream.streamId, stream.containerExtension)
                                    onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_VOD, stream.streamId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VodGridItem(
    title: String,
    posterUrl: String,
    rating: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCardDark)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.8f)),
                                startY = 100f
                            )
                        )
                )
                if (rating > 0) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = AtilfazGold, modifier = Modifier.size(12.dp))
                        Text(
                            String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Icon(
                    Icons.Default.PlayCircle,
                    null,
                    tint = Color.White.copy(0.7f),
                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
