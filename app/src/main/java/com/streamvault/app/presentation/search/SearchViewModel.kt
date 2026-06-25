package com.streamvault.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.api.ApiResult
import com.streamvault.app.data.models.*
import com.streamvault.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val liveResults: List<LiveStream> = emptyList(),
    val vodResults: List<VodStream> = emptyList(),
    val seriesResults: List<SeriesStream> = emptyList(),
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val hasSearched: Boolean = false,
    val error: String? = null
)

enum class SearchFilter(val label: String) {
    ALL("All"), LIVE("Live TV"), VOD("Movies"), SERIES("Series")
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allLive: List<LiveStream> = emptyList()
    private var allVod: List<VodStream> = emptyList()
    private var allSeries: List<SeriesStream> = emptyList()
    private var searchJob: Job? = null

    init { preloadContent() }

    private fun preloadContent() {
        viewModelScope.launch {
            val liveResult = contentRepository.getLiveStreams()
            val vodResult = contentRepository.getVodStreams()
            val seriesResult = contentRepository.getSeries()
            allLive = (liveResult as? ApiResult.Success)?.data ?: emptyList()
            allVod = (vodResult as? ApiResult.Success)?.data ?: emptyList()
            allSeries = (seriesResult as? ApiResult.Success)?.data ?: emptyList()
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    liveResults = emptyList(),
                    vodResults = emptyList(),
                    seriesResults = emptyList(),
                    hasSearched = false
                )
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        val q = query.trim().lowercase()
        _uiState.update { state ->
            state.copy(
                liveResults = allLive.filter { it.name.contains(q, true) },
                vodResults = allVod.filter { it.name.contains(q, true) },
                seriesResults = allSeries.filter { it.name.contains(q, true) },
                hasSearched = true
            )
        }
    }

    fun setFilter(filter: SearchFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun clearSearch() {
        _uiState.update { SearchUiState() }
    }

    suspend fun buildLiveUrl(streamId: Int) = contentRepository.buildLiveStreamUrl(streamId)
    suspend fun buildVodUrl(streamId: Int, ext: String) = contentRepository.buildVodStreamUrl(streamId, ext)
}
