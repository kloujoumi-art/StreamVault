package com.atilfaz.app.presentation.series

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.atilfaz.app.presentation.vod.ContentCategoryChip
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AtilfazBackground)) {
        val columns = when {
            maxWidth > 900.dp -> 5
            maxWidth > 600.dp -> 4
            maxWidth > 400.dp -> 3
            else -> 2
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0A))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VideoLibrary, null, tint = AtilfazBlueLight, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("SERIES", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                if (!state.isLoading) {
                    Spacer(Modifier.width(8.dp))
                    Text("(${state.filteredSeries.size})", fontSize = 12.sp, color = AtilfazTextHint)
                }
            }

            // ── Search ───────────────────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search series...", fontSize = 14.sp, color = AtilfazTextHint) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = AtilfazTextHint, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = AtilfazTextHint, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AtilfazBlueLight, unfocusedBorderColor = AtilfazBorder,
                    focusedTextColor = Color.White, unfocusedTextColor = AtilfazTextSecond,
                    cursorColor = AtilfazBlueLight, focusedContainerColor = AtilfazCard, unfocusedContainerColor = AtilfazCard
                )
            )

            // ── Categories ───────────────────────────────────────────────
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    item { ContentCategoryChip("All", state.selectedCategoryId == "all") { viewModel.selectCategory("all") } }
                    items(state.categories) { cat ->
                        ContentCategoryChip(cat.categoryName, state.selectedCategoryId == cat.categoryId) {
                            viewModel.selectCategory(cat.categoryId)
                        }
                    }
                }
            }

            // ── Grid ─────────────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AtilfazBlueLight, modifier = Modifier.size(40.dp))
                }
            } else if (state.filteredSeries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideoLibrary, null, tint = AtilfazTextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No series found", color = AtilfazTextHint, fontSize = 15.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredSeries) { series ->
                        SeriesCard(
                            title = series.name,
                            coverUrl = series.cover,
                            rating = series.rating5Based,
                            year = series.lastModified?.let { it.take(4) } ?: "",
                            onClick = { onNavigateToSeriesDetail(series.seriesId) }
                        )
                    }
                    item(span = { GridItemSpan(columns) }) { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SeriesCard(title: String, coverUrl: String, rating: Double, year: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCard),
        elevation = CardDefaults.cardElevation(2.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                            startY = 150f
                        )
                    )
                )
                // Year badge
                if (year.isNotEmpty()) {
                    Text(
                        text = year,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(0.55f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                // Rating
                if (rating > 0) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(0.6f))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = AtilfazGold, modifier = Modifier.size(10.dp))
                        Text(String.format("%.1f", rating), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                lineHeight = 16.sp
            )
        }
    }
}
