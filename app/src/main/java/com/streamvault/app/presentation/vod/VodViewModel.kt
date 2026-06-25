package com.streamvault.app.presentation.vod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.api.ApiResult
import com.streamvault.app.data.models.Category
import com.streamvault.app.data.models.VodStream
import com.streamvault.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VodUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val streams: List<VodStream> = emptyList(),
    val filteredStreams: List<VodStream> = emptyList(),
    val selectedCategoryId: String = "all",
    val searchQuery: String = "",
    val sortBy: VodSortOption = VodSortOption.NAME,
    val error: String? = null
)

enum class VodSortOption(val label: String) {
    NAME("Name"), RATING("Rating"), DATE("Date Added")
}

@HiltViewModel
class VodViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VodUiState())
    val uiState: StateFlow<VodUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val catResult = contentRepository.getVodCategories()
            val streamResult = contentRepository.getVodStreams()
            val categories = (catResult as? ApiResult.Success)?.data ?: emptyList()
            val streams = (streamResult as? ApiResult.Success)?.data ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = categories,
                    streams = streams,
                    filteredStreams = streams,
                    error = (streamResult as? ApiResult.Error)?.message
                )
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { state ->
            val base = if (categoryId == "all") state.streams
            else state.streams.filter { it.categoryId == categoryId }
            state.copy(selectedCategoryId = categoryId, filteredStreams = applySort(base, state.sortBy))
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val base = if (state.selectedCategoryId == "all") state.streams
            else state.streams.filter { it.categoryId == state.selectedCategoryId }
            val filtered = if (query.isBlank()) base
            else base.filter { it.name.contains(query, ignoreCase = true) }
            state.copy(searchQuery = query, filteredStreams = applySort(filtered, state.sortBy))
        }
    }

    fun setSortOption(sort: VodSortOption) {
        _uiState.update { state ->
            state.copy(sortBy = sort, filteredStreams = applySort(state.filteredStreams, sort))
        }
    }

    private fun applySort(list: List<VodStream>, sort: VodSortOption): List<VodStream> = when (sort) {
        VodSortOption.NAME -> list.sortedBy { it.name }
        VodSortOption.RATING -> list.sortedByDescending { it.rating5Based }
        VodSortOption.DATE -> list.sortedByDescending { it.added }
    }

    suspend fun buildStreamUrl(streamId: Int, ext: String): String =
        contentRepository.buildVodStreamUrl(streamId, ext)

    fun clearError() = _uiState.update { it.copy(error = null) }
}
