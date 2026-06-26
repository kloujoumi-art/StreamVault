package com.atilfaz.app.presentation.vod

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
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VodScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: (String, String, String, Int) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    viewModel: VodViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSort by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AtilfazBackground)) {
        val isWide = maxWidth > 600.dp
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, null, tint = AtilfazBlueLight, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("MOVIES", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    if (!state.isLoading) {
                        Spacer(Modifier.width(8.dp))
                        Text("(${state.filteredStreams.size})", fontSize = 12.sp, color = AtilfazTextHint)
                    }
                }
                Box {
                    IconButton(onClick = { showSort = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Sort, null, tint = AtilfazTextSecond, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showSort,
                        onDismissRequest = { showSort = false },
                        modifier = Modifier.background(AtilfazCardAlt)
                    ) {
                        VodSortOption.entries.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.label, color = if (state.sortBy == opt) AtilfazBlueLight else Color.White, fontSize = 14.sp) },
                                onClick = { viewModel.setSortOption(opt); showSort = false },
                                leadingIcon = {
                                    if (state.sortBy == opt) Icon(Icons.Default.Check, null, tint = AtilfazBlueLight, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }

            // ── Search ───────────────────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search movies...", fontSize = 14.sp, color = AtilfazTextHint) },
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
            } else if (state.filteredStreams.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MovieFilter, null, tint = AtilfazTextHint, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No movies found", color = AtilfazTextHint, fontSize = 15.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredStreams) { stream ->
                        MovieCard(
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
                    item(span = { GridItemSpan(columns) }) { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun ContentCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) AtilfazBlue else AtilfazCard
    val textColor = if (selected) Color.White else AtilfazTextSecond
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 13.sp, color = textColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}

@Composable
fun MovieCard(title: String, posterUrl: String, rating: Double, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AtilfazCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                            startY = 150f
                        )
                    )
                )
                // Play icon
                Icon(
                    Icons.Default.PlayCircleFilled,
                    null,
                    tint = Color.White.copy(0.75f),
                    modifier = Modifier.size(34.dp).align(Alignment.Center)
                )
                // Rating badge
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
