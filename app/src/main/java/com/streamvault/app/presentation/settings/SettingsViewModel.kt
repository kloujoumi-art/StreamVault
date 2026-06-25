package com.streamvault.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.preferences.UserPreferences
import com.streamvault.app.data.repository.AuthRepository
import com.streamvault.app.data.repository.FavoriteRepository
import com.streamvault.app.data.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "system",
    val autoPlay: Boolean = true,
    val resumePlayback: Boolean = true,
    val videoQuality: String = "auto",
    val serverUrl: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val logoutConfirmation: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.themeMode,
                userPreferences.autoPlay,
                userPreferences.resumePlayback,
                userPreferences.videoQuality
            ) { theme, auto, resume, quality ->
                _uiState.update { it.copy(themeMode = theme, autoPlay = auto, resumePlayback = resume, videoQuality = quality) }
            }.collect {}
        }
        viewModelScope.launch {
            combine(
                userPreferences.serverUrl,
                userPreferences.username
            ) { url, user ->
                _uiState.update { it.copy(serverUrl = url, username = user) }
            }.collect {}
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { userPreferences.setThemeMode(mode) }
    }

    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAutoPlay(enabled) }
    }

    fun setResumePlayback(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setResumePlayback(enabled) }
    }

    fun setVideoQuality(quality: String) {
        viewModelScope.launch { userPreferences.setVideoQuality(quality) }
    }

    fun clearHistory() {
        viewModelScope.launch { watchHistoryRepository.clearAll() }
    }

    fun clearFavorites() {
        viewModelScope.launch { favoriteRepository.clearAll() }
    }

    fun showLogoutConfirmation() = _uiState.update { it.copy(logoutConfirmation = true) }
    fun dismissLogoutConfirmation() = _uiState.update { it.copy(logoutConfirmation = false) }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
