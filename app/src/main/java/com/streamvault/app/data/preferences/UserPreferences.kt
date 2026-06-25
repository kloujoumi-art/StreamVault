package com.streamvault.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.streamvault.app.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val SERVER_URL = stringPreferencesKey(Constants.PREFS_SERVER_URL)
        val USERNAME = stringPreferencesKey(Constants.PREFS_USERNAME)
        val PASSWORD = stringPreferencesKey(Constants.PREFS_PASSWORD)
        val THEME_MODE = stringPreferencesKey(Constants.PREFS_THEME_MODE)
        val AUTO_PLAY = booleanPreferencesKey(Constants.PREFS_AUTO_PLAY)
        val RESUME_PLAYBACK = booleanPreferencesKey(Constants.PREFS_RESUME_PLAYBACK)
        val VIDEO_QUALITY = stringPreferencesKey(Constants.PREFS_VIDEO_QUALITY)
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_STATUS = stringPreferencesKey("user_status")
        val EXP_DATE = stringPreferencesKey("exp_date")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.IS_LOGGED_IN] ?: false }

    val serverUrl: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.SERVER_URL] ?: "" }

    val username: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.USERNAME] ?: "" }

    val password: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.PASSWORD] ?: "" }

    val themeMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.THEME_MODE] ?: "system" }

    val autoPlay: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.AUTO_PLAY] ?: true }

    val resumePlayback: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.RESUME_PLAYBACK] ?: true }

    val videoQuality: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.VIDEO_QUALITY] ?: "auto" }

    suspend fun saveCredentials(serverUrl: String, username: String, password: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = serverUrl
            prefs[Keys.USERNAME] = username
            prefs[Keys.PASSWORD] = password
            prefs[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun saveUserInfo(status: String, expDate: String?) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_STATUS] = status
            if (expDate != null) prefs[Keys.EXP_DATE] = expDate
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_PLAY] = enabled }
    }

    suspend fun setResumePlayback(enabled: Boolean) {
        dataStore.edit { it[Keys.RESUME_PLAYBACK] = enabled }
    }

    suspend fun setVideoQuality(quality: String) {
        dataStore.edit { it[Keys.VIDEO_QUALITY] = quality }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER_URL)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.PASSWORD)
            prefs[Keys.IS_LOGGED_IN] = false
        }
    }

    suspend fun getServerUrlOnce(): String = dataStore.data.first()[Keys.SERVER_URL] ?: ""
    suspend fun getUsernameOnce(): String = dataStore.data.first()[Keys.USERNAME] ?: ""
    suspend fun getPasswordOnce(): String = dataStore.data.first()[Keys.PASSWORD] ?: ""
}
