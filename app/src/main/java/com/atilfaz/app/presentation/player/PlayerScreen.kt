package com.atilfaz.app.presentation.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.atilfaz.app.ui.theme.*
import com.atilfaz.app.utils.Constants
import com.atilfaz.app.utils.rememberIsTV
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
    val isTV = rememberIsTV()
    val focusRequester = remember { FocusRequester() }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = if (streamType == Constants.STREAM_TYPE_LIVE)
                Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        }
    }

    LaunchedEffect(streamUrl) {
        viewModel.initPlayer(streamUrl, streamTitle, streamType, streamId)
        if (streamUrl.isNotEmpty()) {
            exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            if (state.progressMs > 0 && streamType != Constants.STREAM_TYPE_LIVE) {
                exoPlayer.seekTo(state.progressMs)
            }
        }
    }

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

    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            if (exoPlayer.isPlaying) {
                viewModel.onProgressUpdate(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0))
            }
        }
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Auto-demander le focus sur TV pour que D-pad fonctionne dès l'ouverture
    LaunchedEffect(isTV) {
        if (isTV) {
            delay(100)
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusTarget()
            // ── Gestion D-pad pour Android TV ────────────────────────────
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        if (state.showControls) {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        } else {
                            viewModel.showControls()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (streamType != Constants.STREAM_TYPE_LIVE) {
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0))
                            viewModel.showControls()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (streamType != Constants.STREAM_TYPE_LIVE) {
                            exoPlayer.seekTo(exoPlayer.currentPosition + 10_000)
                            viewModel.showControls()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                        viewModel.showControls()
                        true
                    }
                    KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                        if (state.showControls) {
                            viewModel.hideControls()
                        } else {
                            onNavigateBack()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        true
                    }
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        exoPlayer.seekTo(exoPlayer.currentPosition + 30_000)
                        true
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        exoPlayer.seekTo((exoPlayer.currentPosition - 30_000).coerceAtLeast(0))
                        true
                    }
                    else -> false
                }
            }
    ) {
        // ── Vidéo ────────────────────────────────────────────────────────
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

        // ── Superposition tactile (téléphone) ─────────────────────────
        if (!isTV) {
            PhoneTouchOverlay(
                exoPlayer = exoPlayer,
                streamType = streamType,
                durationMs = state.durationMs,
                onShowControls = viewModel::showControls
            )
        }

        // ── Indicateur de chargement ──────────────────────────────────
        if (state.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(if (isTV) 56.dp else 40.dp),
                color = AtilfazAccentCyan,
                strokeWidth = if (isTV) 4.dp else 3.dp
            )
        }

        // ── Contrôles ────────────────────────────────────────────────
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
                isTV = isTV,
                onNavigateBack = onNavigateBack,
                onNavigateToEpg = onNavigateToEpg,
                onToggleFavorite = { viewModel.toggleFavorite("") },
                onHideControls = viewModel::hideControls,
                onShowControls = viewModel::showControls,
                onSpeedChange = viewModel::setPlaybackSpeed
            )
        }

        // ── Aide gestuelle (téléphone uniquement, 3 premières secondes) ──
        if (!isTV) {
            PhoneGestureHint(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

// ── Superposition gestes tactiles (téléphone) ───────────────────────────────

@Composable
private fun PhoneTouchOverlay(
    exoPlayer: ExoPlayer,
    streamType: String,
    durationMs: Long,
    onShowControls: () -> Unit
) {
    var seekOverlay by remember { mutableStateOf<Long?>(null) }
    var dragAccumX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Tap simple → afficher les contrôles
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onShowControls() })
            }
            // Glisser horizontal → avancer / reculer (VOD uniquement)
            .then(
                if (streamType != Constants.STREAM_TYPE_LIVE) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumX = 0f },
                            onDragEnd = {
                                val seekMs = (dragAccumX * 300).toLong()
                                val newPos = (exoPlayer.currentPosition + seekMs).coerceIn(0, durationMs.coerceAtLeast(1))
                                exoPlayer.seekTo(newPos)
                                seekOverlay = seekMs
                                dragAccumX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumX += dragAmount / size.width.toFloat()
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        // Bulle d'indication de seek
        seekOverlay?.let { ms ->
            LaunchedEffect(ms) {
                delay(1200)
                seekOverlay = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val sign = if (ms >= 0) "+" else ""
                Text(
                    text = "$sign${ms / 1000}s",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
        }
    }
}

// ── Indice gestuel affiché 3s au démarrage ─────────────────────────────────

@Composable
private fun PhoneGestureHint(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.padding(bottom = 100.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "← Glisser pour avancer/reculer →",
                color = Color.White.copy(0.8f),
                fontSize = 12.sp
            )
        }
    }
}

// ── Contrôles player ─────────────────────────────────────────────────────────

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun PlayerControls(
    state: PlayerUiState,
    streamType: String,
    streamId: Int,
    exoPlayer: ExoPlayer,
    isTV: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToEpg: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onHideControls: () -> Unit,
    onShowControls: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    val iconSize = if (isTV) 36.dp else 28.dp
    val playBtnSize = if (isTV) 96.dp else 72.dp
    val seekBtnSize = if (isTV) 72.dp else 56.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .then(if (!isTV) Modifier.clickable { onHideControls() } else Modifier)
    ) {
        // ── Barre supérieure ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(if (isTV) 16.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(if (isTV) 56.dp else 44.dp)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(iconSize))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = state.streamTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isTV) 22.sp else 16.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                state.currentProgram?.let { program ->
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isTV) 16.sp else 12.sp
                        ),
                        color = Color.White.copy(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (streamType == Constants.STREAM_TYPE_LIVE) {
                IconButton(
                    onClick = { onNavigateToEpg(streamId) },
                    modifier = Modifier.size(if (isTV) 56.dp else 44.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = AtilfazAccentCyan, modifier = Modifier.size(iconSize))
                }
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(if (isTV) 56.dp else 44.dp)
            ) {
                Icon(
                    if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (state.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // ── Contrôles centraux ────────────────────────────────────────
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(if (isTV) 40.dp else 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (streamType != Constants.STREAM_TYPE_LIVE) {
                IconButton(
                    onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0)) },
                    modifier = Modifier.size(seekBtnSize)
                ) {
                    Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(if (isTV) 48.dp else 36.dp))
                }
            }

            IconButton(
                onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                modifier = Modifier
                    .size(playBtnSize)
                    .clip(RoundedCornerShape(playBtnSize / 2))
                    .background(Color.White.copy(0.2f))
            ) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(if (isTV) 60.dp else 44.dp)
                )
            }

            if (streamType != Constants.STREAM_TYPE_LIVE) {
                IconButton(
                    onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10_000) },
                    modifier = Modifier.size(seekBtnSize)
                ) {
                    Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(if (isTV) 48.dp else 36.dp))
                }
            }
        }

        // ── Barre inférieure ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = if (isTV) 24.dp else 16.dp, start = 8.dp, end = 8.dp)
        ) {
            if (streamType != Constants.STREAM_TYPE_LIVE && state.durationMs > 0) {
                Slider(
                    value = if (state.durationMs > 0) state.progressMs.toFloat() / state.durationMs.toFloat() else 0f,
                    onValueChange = { fraction ->
                        exoPlayer.seekTo((fraction * state.durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AtilfazAccentCyan,
                        activeTrackColor = AtilfazAccentCyan
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatDuration(state.progressMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isTV) 14.sp else 11.sp),
                        color = Color.White
                    )
                    Text(
                        formatDuration(state.durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (isTV) 14.sp else 11.sp),
                        color = Color.White.copy(0.6f)
                    )
                }
            }

            if (!isTV) {
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

            // Indice télécommande TV
            if (isTV) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TvHint("◀◀ Reculer 10s")
                    Spacer(Modifier.width(24.dp))
                    TvHint("OK → Lecture/Pause")
                    Spacer(Modifier.width(24.dp))
                    TvHint("Avancer 10s ▶▶")
                }
            }
        }
    }
}

@Composable
private fun TvHint(text: String) {
    Text(
        text = text,
        color = Color.White.copy(0.5f),
        fontSize = 11.sp,
        letterSpacing = 0.3.sp
    )
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
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = { onSpeedSelected(speed); expanded = false },
                    leadingIcon = {
                        if (speed == currentSpeed) Icon(Icons.Default.Check, null, tint = AtilfazAccentCyan)
                    }
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
