package com.atilfaz.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilfaz.app.data.local.entities.FavoriteEntity
import com.atilfaz.app.data.repository.ContentRepository
import com.atilfaz.app.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val allFavorites: List<FavoriteEntity> = emptyList(),
    val liveFavorites: List<FavoriteEntity> = emptyList(),
    val vodFavorites: List<FavoriteEntity> = emptyList(),
    val seriesFavorites: List<FavoriteEntity> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                favoriteRepository.getAllFavorites(),
                favoriteRepository.getFavoritesByType("live"),
                favoriteRepository.getFavoritesByType("movie"),
                favoriteRepository.getFavoritesByType("series")
            ) { all, live, vod, series ->
                FavoritesUiState(
                    allFavorites = all,
                    liveFavorites = live,
                    vodFavorites = vod,
                    seriesFavorites = series,
                    selectedTab = _uiState.value.selectedTab
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun selectTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }

    fun removeFavorite(streamId: Int, type: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(streamId, type)
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            favoriteRepository.clearAll()
        }
    }

    suspend fun buildStreamUrl(item: FavoriteEntity): String = when (item.streamType) {
        "live" -> contentRepository.buildLiveStreamUrl(item.streamId)
        "movie" -> contentRepository.buildVodStreamUrl(item.streamId, item.containerExtension.ifEmpty { "mkv" })
        else -> item.streamUrl
    }
}
