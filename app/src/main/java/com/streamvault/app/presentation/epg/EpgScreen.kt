package com.streamvault.app.presentation.epg

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamvault.app.data.local.entities.EpgProgramEntity
import com.streamvault.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpgScreen(
    streamId: Int,
    onNavigateBack: () -> Unit,
    viewModel: EpgViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(streamId) { viewModel.loadEpg(streamId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Program Guide", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh(streamId) }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StreamVaultBlack)
            )
        },
        containerColor = StreamVaultBlack
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StreamVaultAccentCyan)
            }
            return@Scaffold
        }

        if (state.programs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No EPG data available", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.4f))
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.programs) { program ->
                EpgProgramCard(
                    program = program,
                    isCurrent = program == state.currentProgram
                )
            }
        }
    }
}

@Composable
private fun EpgProgramCard(
    program: EpgProgramEntity,
    isCurrent: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = timeFormat.format(Date(program.startTimestamp * 1000))
    val endTime = timeFormat.format(Date(program.stopTimestamp * 1000))

    val now = System.currentTimeMillis() / 1000
    val progress = if (isCurrent && program.stopTimestamp > program.startTimestamp) {
        (now - program.startTimestamp).toFloat() / (program.stopTimestamp - program.startTimestamp).toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) StreamVaultAccentCyan.copy(0.15f) else StreamVaultCardDark
        ),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, StreamVaultAccentCyan.copy(0.5f)) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiveBadgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NOW", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isCurrent) Color.White else Color.White.copy(0.9f)
                    )
                }
                Text(
                    text = "$startTime – $endTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCurrent) StreamVaultAccentCyan else Color.White.copy(0.5f)
                )
            }

            if (program.description.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.6f),
                    maxLines = 2
                )
            }

            if (isCurrent && progress > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = StreamVaultAccentCyan,
                    trackColor = Color.White.copy(0.2f)
                )
            }
        }
    }
}
