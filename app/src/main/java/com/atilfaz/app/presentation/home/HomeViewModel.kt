package com.atilfaz.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.local.entities.WatchHistoryEntity
import com.atilfaz.app.data.models.LiveStream
import com.atilfaz.app.data.models.SeriesStream
import com.atilfaz.app.data.models.VodStream
import com.atilfaz.app.data.repository.ContentRepository
import com.atilfaz.app.data.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredLive: List<LiveStream> = emptyList(),
    val recentVod: List<VodStream> = emptyList(),
    val popularSeries: List<SeriesStream> = emptyList(),
    val continueWatching: List<WatchHistoryEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            watchHistoryRepository.getRecentHistory(10).collect { history ->
                _uiState.update { it.copy(continueWatching = history) }
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val liveResult = contentRepository.getLiveStreams()
                val vodResult = contentRepository.getVodStreams()
                val seriesResult = contentRepository.getSeries()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        featuredLive = (liveResult as? ApiResult.Success)?.data?.take(10) ?: state.featuredLive,
                        recentVod = (vodResult as? ApiResult.Success)?.data?.take(20) ?: state.recentVod,
                        popularSeries = (seriesResult as? ApiResult.Success)?.data?.take(20) ?: state.popularSeries
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
