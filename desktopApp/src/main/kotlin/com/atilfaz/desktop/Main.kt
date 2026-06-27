package com.atilfaz.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.atilfaz.desktop.api.XtreamApi
import com.atilfaz.desktop.api.XtreamCredentials
import com.atilfaz.desktop.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.prefs.Preferences

fun main() = application {
    val windowState = rememberWindowState(width = 1280.dp, height = 780.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Atilfaz IPTV",
        state = windowState
    ) {
        MaterialTheme(colorScheme = AppColorScheme) {
            App()
        }
    }
}

@Composable
fun App() {
    val prefs = remember { Preferences.userRoot().node("atilfaz-iptv") }
    val scope = rememberCoroutineScope()

    // Essayer de restaurer les identifiants sauvegardés
    var creds by remember {
        val savedUrl  = prefs.get("server_url", "")
        val savedUser = prefs.get("username", "")
        val savedPass = prefs.get("password", "")
        mutableStateOf(
            if (savedUrl.isNotBlank() && savedUser.isNotBlank() && savedPass.isNotBlank())
                XtreamCredentials(savedUrl, savedUser, savedPass)
            else null
        )
    }
    var api by remember { mutableStateOf<XtreamApi?>(creds?.let { XtreamApi(it) }) }
    var isAuthenticated by remember { mutableStateOf(creds != null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        if (!isAuthenticated || api == null) {
            LoginScreen(
                onLogin = { credentials ->
                    scope.launch {
                        isLoading = true
                        loginError = null
                        val newApi = XtreamApi(credentials)
                        val ok = kotlinx.coroutines.withContext(Dispatchers.IO) { newApi.authenticate() }
                        if (ok) {
                            // Sauvegarder les identifiants
                            prefs.put("server_url", credentials.serverUrl)
                            prefs.put("username", credentials.username)
                            prefs.put("password", credentials.password)
                            api = newApi
                            creds = credentials
                            isAuthenticated = true
                        } else {
                            loginError = "Connexion échouée. Vérifiez l'URL, l'identifiant et le mot de passe."
                        }
                        isLoading = false
                    }
                },
                errorMessage = loginError,
                isLoading = isLoading
            )
        } else {
            MainScreen(
                api = api!!,
                onPlay = { url, title -> launchExternalPlayer(url, title) },
                onLogout = {
                    prefs.remove("server_url")
                    prefs.remove("username")
                    prefs.remove("password")
                    api = null
                    creds = null
                    isAuthenticated = false
                }
            )
        }
    }
}

// ── Lancement du lecteur externe ───────────────────────────────────────────────

fun launchExternalPlayer(url: String, title: String) {
    try {
        // 1. Essayer VLC (le plus commun pour les flux IPTV)
        val vlcPaths = listOf(
            "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
            "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe",
            "/usr/bin/vlc",
            "/Applications/VLC.app/Contents/MacOS/VLC"
        )
        val vlc = vlcPaths.firstOrNull { File(it).exists() }
        if (vlc != null) {
            ProcessBuilder(vlc, url, "--meta-title=$title").start()
            return
        }

        // 2. Essayer MPV
        val mpvPaths = listOf("C:\\Program Files\\mpv\\mpv.exe", "/usr/bin/mpv")
        val mpv = mpvPaths.firstOrNull { File(it).exists() }
        if (mpv != null) {
            ProcessBuilder(mpv, url, "--title=$title").start()
            return
        }

        // 3. Essayer MPC-HC
        val mpcPaths = listOf(
            "C:\\Program Files\\MPC-HC\\mpc-hc64.exe",
            "C:\\Program Files (x86)\\K-Lite Codec Pack\\MPC-HC64\\mpc-hc64.exe"
        )
        val mpc = mpcPaths.firstOrNull { File(it).exists() }
        if (mpc != null) {
            ProcessBuilder(mpc, url).start()
            return
        }

        // 4. Fallback : lecteur par défaut du système
        Desktop.getDesktop().browse(URI(url))
    } catch (_: Exception) {
        // Ouvrir dans le navigateur comme dernier recours
        try { Desktop.getDesktop().browse(URI(url)) } catch (_: Exception) {}
    }
}
