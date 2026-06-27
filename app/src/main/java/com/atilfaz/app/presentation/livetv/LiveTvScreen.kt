package com.atilfaz.app.presentation.livetv

import androidx.compose.foundation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import com.atilfaz.app.utils.handleDpadAction
import com.atilfaz.app.utils.rememberIsTV
import kotlinx.coroutines.launch

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
    val isTV = rememberIsTV()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AtilfazBackground)) {
        val isWide = maxWidth > 600.dp

        if (isWide) {
            // ── TV / Tablette / Paysage : deux panneaux ──────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                CategorySidebar(
                    categories = state.categories.map { it.categoryId to it.categoryName },
                    selected = state.selectedCategoryId,
                    onSelect = viewModel::selectCategory,
                    isTV = isTV,
                    modifier = Modifier.width(if (isTV) 240.dp else 200.dp).fillMaxHeight()
                )
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AtilfazBorder))
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (!isTV) {
                        SearchBar(
                            query = state.searchQuery,
                            onQuery = viewModel::onSearchQueryChange,
                            placeholder = "Rechercher une chaîne...",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    ChannelList(
                        streams = state.filteredStreams,
                        isLoading = state.isLoading,
                        isTV = isTV,
                        onPlay = { stream ->
                            scope.launch {
                                val url = viewModel.buildStreamUrl(stream.streamId)
                                onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_LIVE, stream.streamId)
                            }
                        },
                        onEpg = { onNavigateToEpg(it) }
                    )
                }
            }
        } else {
            // ── Téléphone / Portrait : layout empilé ────────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tv, null, tint = AtilfazBlueLight, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("LIVE TV", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                }
                SearchBar(
                    query = state.searchQuery,
                    onQuery = viewModel::onSearchQueryChange,
                    placeholder = "Rechercher une chaîne...",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                if (state.categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            CategoryChip("Tout", state.selectedCategoryId == "all") { viewModel.selectCategory("all") }
                        }
                        items(state.categories) { cat ->
                            CategoryChip(cat.categoryName, state.selectedCategoryId == cat.categoryId) {
                                viewModel.selectCategory(cat.categoryId)
                            }
                        }
                    }
                }
                ChannelList(
                    streams = state.filteredStreams,
                    isLoading = state.isLoading,
                    isTV = false,
                    onPlay = { stream ->
                        scope.launch {
                            val url = viewModel.buildStreamUrl(stream.streamId)
                            onNavigateToPlayer(url, stream.name, Constants.STREAM_TYPE_LIVE, stream.streamId)
                        }
                    },
                    onEpg = { onNavigateToEpg(it) }
                )
            }
        }
    }
}

@Composable
private fun CategorySidebar(
    categories: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    isTV: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(Color(0xFF080808)),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tv, null, tint = AtilfazBlueLight, modifier = Modifier.size(if (isTV) 22.dp else 18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "LIVE TV",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTV) 16.sp else 13.sp,
                    color = Color.White
                )
            }
            HorizontalDivider(color = AtilfazBorder)
        }
        item {
            SidebarItem("Tout", selected == "all", isTV) { onSelect("all") }
        }
        items(categories) { (id, name) ->
            SidebarItem(name, selected == id, isTV) { onSelect(id) }
        }
    }
}

@Composable
private fun SidebarItem(label: String, selected: Boolean, isTV: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val highlight = selected || isFocused
    val bg = when {
        selected -> AtilfazBlue.copy(alpha = 0.25f)
        isFocused -> AtilfazBlue.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val textColor = if (highlight) AtilfazBlueLight else AtilfazTextSecond
    val borderColor = if (isFocused && !selected) AtilfazBlueLight else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .border(1.dp, borderColor)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .handleDpadAction(onClick)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = if (isTV) 16.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selected) {
            Box(modifier = Modifier.width(3.dp).height(if (isTV) 20.dp else 16.dp).clip(RoundedCornerShape(2.dp)).background(AtilfazBlueLight))
        } else {
            Spacer(Modifier.width(3.dp))
        }
        Text(
            text = label,
            fontSize = if (isTV) 16.sp else 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) AtilfazBlue else AtilfazCard
    val textColor = if (selected) Color.White else AtilfazTextSecond
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(label, fontSize = 13.sp, color = textColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = 14.sp, color = AtilfazTextHint) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = AtilfazTextHint, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) {
                    Icon(Icons.Default.Clear, null, tint = AtilfazTextHint, modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AtilfazBlueLight,
            unfocusedBorderColor = AtilfazBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = AtilfazTextSecond,
            cursorColor = AtilfazBlueLight,
            focusedContainerColor = AtilfazCard,
            unfocusedContainerColor = AtilfazCard
        )
    )
}

@Composable
private fun ChannelList(
    streams: List<com.atilfaz.app.data.models.LiveStream>,
    isLoading: Boolean,
    isTV: Boolean,
    onPlay: (com.atilfaz.app.data.models.LiveStream) -> Unit,
    onEpg: (Int) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AtilfazBlueLight, modifier = Modifier.size(if (isTV) 56.dp else 40.dp))
        }
        return
    }
    if (streams.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, tint = AtilfazTextHint, modifier = Modifier.size(if (isTV) 64.dp else 48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Aucune chaîne trouvée", color = AtilfazTextHint, fontSize = if (isTV) 18.sp else 15.sp)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = if (isTV) 16.dp else 92.dp)) {
        itemsIndexed(streams) { index, stream ->
            ChannelItem(
                index = index + 1,
                name = stream.name,
                iconUrl = stream.streamIcon,
                hasEpg = !stream.epgChannelId.isNullOrEmpty(),
                hasArchive = stream.tvArchive == 1,
                isTV = isTV,
                onClick = { onPlay(stream) },
                onEpgClick = { onEpg(stream.streamId) }
            )
            if (index < streams.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = AtilfazDivider
                )
            }
        }
    }
}

@Composable
private fun ChannelItem(
    index: Int,
    name: String,
    iconUrl: String,
    hasEpg: Boolean,
    hasArchive: Boolean,
    isTV: Boolean,
    onClick: () -> Unit,
    onEpgClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = if (isFocused) AtilfazBlue.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isFocused) AtilfazBlueLight else Color.Transparent
    val logoSize = if (isTV) 60.dp else 48.dp
    val verticalPad = if (isTV) 14.dp else 10.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .handleDpadAction(onClick)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = verticalPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Numéro
        Text(
            text = index.toString(),
            fontSize = if (isTV) 14.sp else 12.sp,
            color = AtilfazTextHint,
            modifier = Modifier.width(if (isTV) 36.dp else 28.dp),
            fontWeight = FontWeight.Medium
        )
        // Logo
        Box(
            modifier = Modifier
                .size(logoSize)
                .clip(RoundedCornerShape(if (isTV) 10.dp else 8.dp))
                .background(AtilfazCard),
            contentAlignment = Alignment.Center
        ) {
            if (iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Tv, null, tint = AtilfazTextHint, modifier = Modifier.size(if (isTV) 32.dp else 24.dp))
            }
        }
        // Nom + badges
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = if (isTV) 17.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 3.dp)) {
                Badge(text = "LIVE", color = AtilfazLiveRed)
                if (hasArchive) Badge(text = "REPLAY", color = AtilfazBlue)
            }
        }
        // EPG
        if (hasEpg) {
            IconButton(onClick = onEpgClick, modifier = Modifier.size(if (isTV) 44.dp else 32.dp)) {
                Icon(Icons.Default.Info, null, tint = AtilfazTextHint, modifier = Modifier.size(if (isTV) 24.dp else 18.dp))
            }
        }
        Icon(
            Icons.Default.PlayCircleOutline,
            null,
            tint = if (isFocused) AtilfazBlueLight else AtilfazBlueLight.copy(alpha = 0.7f),
            modifier = Modifier.size(if (isTV) 36.dp else 28.dp)
        )
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}
