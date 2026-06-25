package com.atilfaz.app.presentation.series

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.atilfaz.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSeriesDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Series", color = Color.White) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search series...") },
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
                    items(state.filteredSeries) { series ->
                        SeriesGridItem(
                            title = series.name,
                            coverUrl = series.cover,
                            rating = series.rating5Based,
                            genre = series.genre,
                            onClick = { onNavigateToSeriesDetail(series.seriesId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesGridItem(
    title: String,
    coverUrl: String,
    rating: Double,
    genre: String,
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
                    model = coverUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.8f)),
                            startY = 100f
                        )
                    )
                )
                if (rating > 0) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = AtilfazGold, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(String.format("%.1f", rating), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
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
