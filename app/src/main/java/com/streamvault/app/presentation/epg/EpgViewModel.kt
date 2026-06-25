package com.streamvault.app.presentation.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.data.local.entities.EpgProgramEntity
import com.streamvault.app.data.repository.EpgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpgUiState(
    val isLoading: Boolean = false,
    val programs: List<EpgProgramEntity> = emptyList(),
    val currentProgram: EpgProgramEntity? = null,
    val error: String? = null
)

@HiltViewModel
class EpgViewModel @Inject constructor(
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgUiState())
    val uiState: StateFlow<EpgUiState> = _uiState.asStateFlow()

    fun loadEpg(streamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            epgRepository.fetchAndCacheEpg(streamId)
            epgRepository.getEpgForStream(streamId).collect { programs ->
                val now = System.currentTimeMillis() / 1000
                val current = programs.firstOrNull {
                    it.startTimestamp <= now && it.stopTimestamp >= now
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        programs = programs,
                        currentProgram = current
                    )
                }
            }
        }
    }

    fun refresh(streamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            epgRepository.fetchAndCacheEpg(streamId, forceRefresh = true)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
