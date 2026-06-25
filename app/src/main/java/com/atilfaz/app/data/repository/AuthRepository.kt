package com.atilfaz.app.data.repository

import com.atilfaz.app.data.api.ApiResult
import com.atilfaz.app.data.api.XtreamApiService
import com.atilfaz.app.data.api.safeApiCall
import com.atilfaz.app.data.models.AuthResponse
import com.atilfaz.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: XtreamApiService,
    private val userPreferences: UserPreferences
) {
    suspend fun login(
        serverUrl: String,
        username: String,
        password: String
    ): ApiResult<AuthResponse> {
        // Save server URL BEFORE the API call so the OkHttp interceptor can read it
        userPreferences.saveServerUrl(serverUrl)
        return safeApiCall {
            val response = apiService.authenticate(username, password)
            if (!response.isSuccessful) {
                throw Exception("Server returned ${response.code()}")
            }
            val body = response.body()
                ?: throw Exception("Empty response from server")
            if (body.userInfo?.auth == 0) {
                throw Exception("Invalid credentials")
            }
            userPreferences.saveCredentials(serverUrl, username, password)
            userPreferences.saveUserInfo(
                status = body.userInfo?.status ?: "",
                expDate = body.userInfo?.expDate
            )
            body
        }
    }

    suspend fun logout() {
        userPreferences.logout()
    }

    val isLoggedIn: Flow<Boolean> = userPreferences.isLoggedIn
    val serverUrl: Flow<String> = userPreferences.serverUrl
    val username: Flow<String> = userPreferences.username
    val password: Flow<String> = userPreferences.password
}
