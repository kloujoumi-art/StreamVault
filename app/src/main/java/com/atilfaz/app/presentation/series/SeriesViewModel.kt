package com.atilfaz.app.presentation.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.models.*
import com.atilfaz.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val series: List<SeriesStream> = emptyList(),
    val filteredSeries: List<SeriesStream> = emptyList(),
    val selectedCategoryId: String = "all",
    val searchQuery: String = "",
    val error: String? = null
)

data class SeriesDetailUiState(
    val isLoading: Boolean = false,
    val seriesInfo: SeriesInfoResponse? = null,
    val selectedSeason: Int = 1,
    val error: String? = null
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(SeriesDetailUiState())
    val detailState: StateFlow<SeriesDetailUiState> = _detailState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val catResult = contentRepository.getSeriesCategories()
            val seriesResult = contentRepository.getSeries()
            val categories = (catResult as? ApiResult.Success)?.data ?: emptyList()
            val series = (seriesResult as? ApiResult.Success)?.data ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    series = series,
                    filteredSeries = series,
                    error = (seriesResult as? ApiResult.Error)?.message
                )
            }
        }
    }

    fun loadSeriesDetail(seriesId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            when (val result = contentRepository.getSeriesInfo(seriesId)) {
                is ApiResult.Success -> _detailState.update {
                    it.copy(isLoading = false, seriesInfo = result.data)
                }
                is ApiResult.Error -> _detailState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { state ->
            val filtered = if (categoryId == "all") state.series
            else state.series.filter { it.categoryId == categoryId }
            state.copy(selectedCategoryId = categoryId, filteredSeries = filtered)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val base = if (state.selectedCategoryId == "all") state.series
            else state.series.filter { it.categoryId == state.selectedCategoryId }
            val filtered = if (query.isBlank()) base
            else base.filter { it.name.contains(query, ignoreCase = true) }
            state.copy(searchQuery = query, filteredSeries = filtered)
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _detailState.update { it.copy(selectedSeason = seasonNumber) }
    }

    suspend fun buildEpisodeUrl(episodeId: String, ext: String): String =
        contentRepository.buildSeriesEpisodeUrl(episodeId, ext)

    fun clearError() = _uiState.update { it.copy(error = null) }
}
