package com.streamvault.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.local.entities.EpgProgramEntity
import com.streamvault.app.data.repository.EpgRepository
import com.streamvault.app.data.repository.FavoriteRepository
import com.streamvault.app.data.repository.WatchHistoryRepository
import com.streamvault.app.data.local.entities.FavoriteEntity
import com.streamvault.app.data.models.ContentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamUrl: String = "",
    val streamTitle: String = "",
    val streamType: String = "live",
    val streamId: Int = 0,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val showControls: Boolean = true,
    val isFullscreen: Boolean = true,
    val isFavorite: Boolean = false,
    val currentProgram: EpgProgramEntity? = null,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watchHistoryRepository: WatchHistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controlsHideJob: Job? = null
    private var progressSaveJob: Job? = null

    fun initPlayer(streamUrl: String, title: String, type: String, streamId: Int) {
        _uiState.update {
            it.copy(
                streamUrl = streamUrl,
                streamTitle = title,
                streamType = type,
                streamId = streamId
            )
        }
        checkFavoriteStatus(streamId, type)
        if (type == "live") fetchCurrentEpg(streamId)
        restoreProgress(streamId, type)
        startProgressSaveLoop()
    }

    private fun checkFavoriteStatus(streamId: Int, type: String) {
        viewModelScope.launch {
            favoriteRepository.isFavorite(streamId, type).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    private fun fetchCurrentEpg(streamId: Int) {
        viewModelScope.launch {
            epgRepository.fetchAndCacheEpg(streamId)
            epgRepository.getEpgForStream(streamId).collect { programs ->
                val now = System.currentTimeMillis() / 1000
                val current = programs.firstOrNull { it.startTimestamp <= now && it.stopTimestamp >= now }
                _uiState.update { it.copy(currentProgram = current) }
            }
        }
    }

    private fun restoreProgress(streamId: Int, type: String) {
        viewModelScope.launch {
            val history = watchHistoryRepository.getProgress(streamId, type)
            if (history != null && history.progressMs > 0) {
                _uiState.update { it.copy(progressMs = history.progressMs, durationMs = history.durationMs) }
            }
        }
    }

    private fun startProgressSaveLoop() {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            while (true) {
                delay(10_000)
                val state = _uiState.value
                if (state.progressMs > 0 && state.streamType != "live") {
                    watchHistoryRepository.saveProgress(
                        streamId = state.streamId,
                        title = state.streamTitle,
                        thumbnailUrl = "",
                        streamType = state.streamType,
                        streamUrl = state.streamUrl,
                        progressMs = state.progressMs,
                        durationMs = state.durationMs
                    )
                }
            }
        }
    }

    fun onPlaybackStateChange(isPlaying: Boolean, isBuffering: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying, isBuffering = isBuffering) }
    }

    fun onProgressUpdate(positionMs: Long, durationMs: Long) {
        _uiState.update { it.copy(progressMs = positionMs, durationMs = durationMs) }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(error = message, isBuffering = false) }
    }

    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
        scheduleControlsHide()
    }

    fun hideControls() {
        _uiState.update { it.copy(showControls = false) }
        controlsHideJob?.cancel()
    }

    private fun scheduleControlsHide() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(showControls = false) }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleFavorite(thumbnailUrl: String) {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.isFavorite) {
                favoriteRepository.removeFavorite(state.streamId, state.streamType)
            } else {
                favoriteRepository.addFavorite(
                    FavoriteEntity(
                        streamId = state.streamId,
                        title = state.streamTitle,
                        thumbnailUrl = thumbnailUrl,
                        streamType = state.streamType,
                        streamUrl = state.streamUrl
                    )
                )
            }
        }
    }

    fun saveProgressOnStop() {
        val state = _uiState.value
        if (state.progressMs > 0 && state.streamType != "live") {
            viewModelScope.launch {
                watchHistoryRepository.saveProgress(
                    streamId = state.streamId,
                    title = state.streamTitle,
                    thumbnailUrl = "",
                    streamType = state.streamType,
                    streamUrl = state.streamUrl,
                    progressMs = state.progressMs,
                    durationMs = state.durationMs
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        saveProgressOnStop()
        controlsHideJob?.cancel()
        progressSaveJob?.cancel()
    }
}
