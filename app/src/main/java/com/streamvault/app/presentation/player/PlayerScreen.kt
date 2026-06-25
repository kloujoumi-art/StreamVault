package com.streamvault.app.presentation.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.streamvault.app.ui.theme.*
import com.streamvault.app.utils.Constants
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    streamTitle: String,
    streamType: String,
    streamId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEpg: (Int) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = if (streamType == Constants.STREAM_TYPE_LIVE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        }
    }

    LaunchedEffect(streamUrl) {
        viewModel.initPlayer(streamUrl, streamTitle, streamType, streamId)
        if (streamUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            if (state.progressMs > 0 && streamType != Constants.STREAM_TYPE_LIVE) {
                exoPlayer.seekTo(state.progressMs)
            }
        }
    }

    // Listen to playback state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                viewModel.onPlaybackStateChange(
                    isPlaying = exoPlayer.isPlaying,
                    isBuffering = playbackState == Player.STATE_BUFFERING
                )
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.onPlaybackStateChange(isPlaying, false)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            viewModel.saveProgressOnStop()
            exoPlayer.release()
        }
    }

    // Update progress
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            if (exoPlayer.isPlaying) {
                viewModel.onProgressUpdate(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0))
            }
        }
    }

    // Lock orientation
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.showControls() }
    ) {
        // Video Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (state.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = StreamVaultAccentCyan
            )
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerControls(
                state = state,
                streamType = streamType,
                streamId = streamId,
                exoPlayer = exoPlayer,
                onNavigateBack = onNavigateBack,
                onNavigateToEpg = onNavigateToEpg,
                onToggleFavorite = { viewModel.toggleFavorite("") },
                onHideControls = viewModel::hideControls,
                onShowControls = viewModel::showControls,
                onSpeedChange = viewModel::setPlaybackSpeed
            )
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun PlayerControls(
    state: PlayerUiState,
    streamType: String,
    streamId: Int,
    exoPlayer: ExoPlayer,
    onNavigateBack: () -> Unit,
    onNavigateToEpg: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onHideControls: () -> Unit,
    onShowControls: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = state.streamTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                state.currentProgram?.let { program ->
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (streamType == Constants.STREAM_TYPE_LIVE) {
                IconButton(onClick = { onNavigateToEpg(streamId) }) {
                    Icon(Icons.Default.CalendarToday, null, tint = StreamVaultAccentCyan)
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (state.isFavorite) Color.Red else Color.White
                )
            }
        }

        // Center Controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (streamType != Constants.STREAM_TYPE_LIVE) {
                IconButton(
                    onClick = { exoPlayer.seekBack() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White.copy(0.2f))
            ) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            if (streamType != Constants.STREAM_TYPE_LIVE) {
                IconButton(
                    onClick = { exoPlayer.seekForward() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }

        // Bottom Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
        ) {
            if (streamType != Constants.STREAM_TYPE_LIVE && state.durationMs > 0) {
                Slider(
                    value = if (state.durationMs > 0) state.progressMs.toFloat() / state.durationMs.toFloat() else 0f,
                    onValueChange = { fraction ->
                        exoPlayer.seekTo((fraction * state.durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = StreamVaultAccentCyan,
                        activeTrackColor = StreamVaultAccentCyan
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDuration(state.progressMs), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Text(formatDuration(state.durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                SpeedSelector(
                    currentSpeed = state.playbackSpeed,
                    onSpeedSelected = { speed ->
                        onSpeedChange(speed)
                        exoPlayer.setPlaybackSpeed(speed)
                    }
                )
            }
        }
    }
}

@Composable
private fun SpeedSelector(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("${currentSpeed}x", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = {
                        onSpeedSelected(speed)
                        expanded = false
                    },
                    leadingIcon = {
                        if (speed == currentSpeed) {
                            Icon(Icons.Default.Check, null, tint = StreamVaultAccentCyan)
                        }
                    }
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
