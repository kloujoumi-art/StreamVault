package com.atilfaz.app.presentation.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.models.Category
import com.atilfaz.app.data.models.LiveStream
import com.atilfaz.app.data.repository.ContentRepository
import com.atilfaz.app.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTvUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val streams: List<LiveStream> = emptyList(),
    val filteredStreams: List<LiveStream> = emptyList(),
    val selectedCategoryId: String = "all",
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val catResult = contentRepository.getLiveCategories()
            val streamResult = contentRepository.getLiveStreams()

            val categories = (catResult as? ApiResult.Success)?.data ?: emptyList()
            val streams = (streamResult as? ApiResult.Success)?.data ?: emptyList()
            val error = (catResult as? ApiResult.Error)?.message
                ?: (streamResult as? ApiResult.Error)?.message

            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    streams = streams,
                    filteredStreams = streams,
                    error = error
                )
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { state ->
            val filtered = if (categoryId == "all") state.streams
            else state.streams.filter { it.categoryId == categoryId }
            state.copy(selectedCategoryId = categoryId, filteredStreams = filtered)
        }
        applySearch()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applySearch()
    }

    private fun applySearch() {
        _uiState.update { state ->
            val base = if (state.selectedCategoryId == "all") state.streams
            else state.streams.filter { it.categoryId == state.selectedCategoryId }
            val filtered = if (state.searchQuery.isBlank()) base
            else base.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
            state.copy(filteredStreams = filtered)
        }
    }

    suspend fun buildStreamUrl(streamId: Int): String =
        contentRepository.buildLiveStreamUrl(streamId)

    fun clearError() = _uiState.update { it.copy(error = null) }
}
