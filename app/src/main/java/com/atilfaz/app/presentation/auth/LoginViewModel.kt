package com.atilfaz.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn.first()
            if (isLoggedIn) {
                _uiState.value = _uiState.value.copy(isSuccess = true)
            }
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url, error = null)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun login() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val normalizedUrl = normalizeUrl(state.serverUrl)
            when (val result = authRepository.login(normalizedUrl, state.username, state.password)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validate(state: LoginUiState): Boolean {
        return when {
            state.serverUrl.isBlank() -> {
                _uiState.value = state.copy(error = "Server URL is required")
                false
            }
            state.username.isBlank() -> {
                _uiState.value = state.copy(error = "Username is required")
                false
            }
            state.password.isBlank() -> {
                _uiState.value = state.copy(error = "Password is required")
                false
            }
            else -> true
        }
    }

    private fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://$normalized"
        }
        return normalized.trimEnd('/')
    }
}
