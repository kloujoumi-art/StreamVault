package com.atilfaz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.atilfaz.app.data.preferences.UserPreferences
import com.atilfaz.app.navigation.NavGraph
import com.atilfaz.app.navigation.Screen
import com.atilfaz.app.ui.theme.AtilfazTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()

        val isLoggedIn = runBlocking { userPreferences.isLoggedIn.first() }
        val themeMode = runBlocking { userPreferences.themeMode.first() }
        isReady = true

        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            AtilfazTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
