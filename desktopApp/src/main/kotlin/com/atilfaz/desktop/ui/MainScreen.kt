package com.atilfaz.desktop.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atilfaz.desktop.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class DesktopTab { LIVE, MOVIES, SERIES, SETTINGS }

@Composable
fun MainScreen(
    api: XtreamApi,
    onPlay: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(DesktopTab.LIVE) }

    Row(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // ── Rail de navigation gauche ──────────────────────────────────────
        NavigationRail(modifier = Modifier.width(80.dp).fillMaxHeight(), containerColor = Color(0xFF080808)) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(BlueMain).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircleFilled, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(24.dp))

            NavigationRailItem(
                selected = currentTab == DesktopTab.LIVE,
                onClick = { currentTab = DesktopTab.LIVE },
                icon = { Icon(Icons.Default.Tv, null) },
                label = { Text("Live", fontSize = 10.sp) }
            )
            NavigationRailItem(
                selected = currentTab == DesktopTab.MOVIES,
                onClick = { currentTab = DesktopTab.MOVIES },
                icon = { Icon(Icons.Default.Movie, null) },
                label = { Text("Films", fontSize = 10.sp) }
            )
            NavigationRailItem(
                selected = currentTab == DesktopTab.SERIES,
                onClick = { currentTab = DesktopTab.SERIES },
                icon = { Icon(Icons.Default.VideoLibrary, null) },
                label = { Text("Séries", fontSize = 10.sp) }
            )
            Spacer(Modifier.weight(1f))
            NavigationRailItem(
                selected = currentTab == DesktopTab.SETTINGS,
                onClick = { currentTab = DesktopTab.SETTINGS },
                icon = { Icon(Icons.Default.Settings, null) },
                label = { Text("Config", fontSize = 10.sp) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (currentTab) {
                DesktopTab.LIVE     -> LiveTab(api, onPlay)
                DesktopTab.MOVIES   -> MoviesTab(api, onPlay)
                DesktopTab.SERIES   -> SeriesTab(api)
                DesktopTab.SETTINGS -> SettingsTab(onLogout)
            }
        }
    }
}

// ── Onglet Live TV ────────────────────────────────────────────────────────────

@Composable
fun LiveTab(api: XtreamApi, onPlay: (String, String) -> Unit) {
    var categories by remember { mutableStateOf<List<LiveCategory>>(emptyList()) }
    var streams by remember { mutableStateOf<List<LiveStream>>(emptyList()) }
    var selectedCat by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCat) {
        isLoading = true
        withContext(Dispatchers.IO) {
            if (categories.isEmpty()) categories = api.getLiveCategories()
            streams = api.getLiveStreams(selectedCat)
        }
        isLoading = false
    }

    val filtered = streams.filter { it.name.contains(search, ignoreCase = true) }

    Row(Modifier.fillMaxSize()) {
        // Sidebar catégories
        LazyColumn(
            modifier = Modifier.width(220.dp).fillMaxHeight().background(Color(0xFF080808)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { SidebarHeader("LIVE TV", Icons.Default.Tv) }
            item { SidebarCatItem("Tout", selectedCat == null) { selectedCat = null } }
            items(categories) { cat ->
                SidebarCatItem(cat.categoryName, selectedCat == cat.categoryId) {
                    selectedCat = cat.categoryId
                }
            }
        }

        Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))

        Column(Modifier.weight(1f).fillMaxHeight()) {
            SearchField(search, { search = it }, "Rechercher une chaîne...")
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueLight)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune chaîne trouvée", color = TextHint, fontSize = 15.sp)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    itemsIndexed(filtered) { i, stream ->
                        DesktopChannelRow(
                            number = i + 1,
                            name = stream.name,
                            hasArchive = stream.tvArchive == 1,
                            onClick = { onPlay(api.buildLiveUrl(stream.streamId), stream.name) }
                        )
                        if (i < filtered.lastIndex) {
                            HorizontalDivider(color = Color(0xFF1F1F1F))
                        }
                    }
                }
            }
        }
    }
}

// ── Onglet Films ──────────────────────────────────────────────────────────────

@Composable
fun MoviesTab(api: XtreamApi, onPlay: (String, String) -> Unit) {
    var movies by remember { mutableStateOf<List<VodStream>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { movies = api.getVodStreams() }
        isLoading = false
    }

    val filtered = movies.filter { it.name.contains(search, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0A)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Movie, null, tint = BlueLight, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("FILMS", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
            Spacer(Modifier.weight(1f))
            Text("${filtered.size} films", fontSize = 12.sp, color = TextHint)
        }
        SearchField(search, { search = it }, "Rechercher un film...")
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueLight)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(count = filtered.size) { index ->
                    val vod = filtered[index]
                    VodCard(vod) {
                        val url = api.buildVodUrl(vod.streamId, vod.containerExtension.ifEmpty { "mkv" })
                        onPlay(url, vod.name)
                    }
                }
            }
        }
    }
}

// ── Onglet Séries ─────────────────────────────────────────────────────────────

@Composable
fun SeriesTab(api: XtreamApi) {
    var series by remember { mutableStateOf<List<SeriesStream>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { series = api.getSeries() }
        isLoading = false
    }

    val filtered = series.filter { it.name.contains(search, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0A)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VideoLibrary, null, tint = BlueLight, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("SÉRIES", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
        }
        SearchField(search, { search = it }, "Rechercher une série...")
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueLight)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(count = filtered.size) { index ->
                    SeriesCard(filtered[index])
                }
            }
        }
    }
}

// ── Onglet Paramètres ─────────────────────────────────────────────────────────

@Composable
fun SettingsTab(onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Icon(Icons.Default.Settings, null, tint = TextHint, modifier = Modifier.size(56.dp))
            Text("Paramètres", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                border = BorderStroke(1.dp, LiveRed)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Se déconnecter / Changer de compte")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Le lecteur vidéo par défaut (VLC recommandé)\nsera utilisé pour lire les flux.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Composants réutilisables ──────────────────────────────────────────────────

@Composable
private fun SidebarHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BlueLight, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
    }
    HorizontalDivider(color = BorderColor)
}

@Composable
private fun SidebarCatItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) BlueMain.copy(alpha = 0.25f) else Color.Transparent
    val color = if (selected) BlueLight else TextSecondary
    Row(
        modifier = Modifier.fillMaxWidth().background(bg).clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Box(Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(BlueLight))
        } else {
            Spacer(Modifier.width(3.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 13.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        placeholder = { Text(placeholder, color = TextHint) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextHint) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton({ onQuery("") }) {
                    Icon(Icons.Default.Clear, null, tint = TextHint)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BlueLight,
            unfocusedBorderColor = BorderColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = TextSecondary,
            cursorColor = BlueLight,
            focusedContainerColor = CardColor,
            unfocusedContainerColor = CardColor
        )
    )
}

@Composable
private fun DesktopChannelRow(number: Int, name: String, hasArchive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("$number", fontSize = 12.sp, color = TextHint, modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallBadge("LIVE", LiveRed)
                if (hasArchive) SmallBadge("REPLAY", BlueMain)
            }
        }
        Icon(Icons.Default.PlayCircleOutline, null, tint = BlueLight, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun VodCard(vod: VodStream, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Movie, null, tint = TextHint, modifier = Modifier.size(48.dp))
            }
            Column(Modifier.padding(10.dp)) {
                Text(vod.name, fontSize = 12.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (vod.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Star, null, tint = GoldColor, modifier = Modifier.size(11.dp))
                        Text(String.format("%.1f", vod.rating), fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesCard(series: SeriesStream) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.VideoLibrary, null, tint = TextHint, modifier = Modifier.size(48.dp))
            }
            Text(
                series.name, fontSize = 12.sp, color = Color.White, maxLines = 2,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun SmallBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(0.18f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
