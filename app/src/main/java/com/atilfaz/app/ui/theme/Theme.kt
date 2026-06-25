package com.atilfaz.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ATILFAZ always uses dark theme (like IPTV Smarters Pro)
private val AtilfazDarkColors = darkColorScheme(
    primary           = AtilfazBlueLight,
    onPrimary         = Color.White,
    primaryContainer  = AtilfazBlueDark,
    onPrimaryContainer = Color.White,
    secondary         = AtilfazBlueAccent,
    onSecondary       = Color.White,
    secondaryContainer = AtilfazBlue,
    onSecondaryContainer = Color.White,
    tertiary          = AtilfazGold,
    onTertiary        = Color.Black,
    tertiaryContainer = Color(0xFF332D00),
    onTertiaryContainer = AtilfazGold,
    error             = AtilfazLiveRed,
    onError           = Color.White,
    errorContainer    = Color(0xFF410002),
    onErrorContainer  = Color(0xFFFFDAD6),
    background        = AtilfazBackground,
    onBackground      = AtilfazTextPrimary,
    surface           = AtilfazSurface,
    onSurface         = AtilfazTextPrimary,
    surfaceVariant    = AtilfazCard,
    onSurfaceVariant  = AtilfazTextSecond,
    outline           = AtilfazBorder,
    outlineVariant    = AtilfazDivider,
    scrim             = Color.Black,
    inverseSurface    = Color(0xFFE0E0E0),
    inverseOnSurface  = Color(0xFF0D0D0D),
    inversePrimary    = AtilfazBlue,
    surfaceTint       = AtilfazBlueLight
)

private val AtilfazLightColors = lightColorScheme(
    primary           = AtilfazBlue,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFBBDEFB),
    onPrimaryContainer = AtilfazBlueDark,
    secondary         = AtilfazBlueLight,
    onSecondary       = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = AtilfazBlueDark,
    tertiary          = Color(0xFFE65100),
    onTertiary        = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFBF360C),
    error             = Color(0xFFBA1A1A),
    onError           = Color.White,
    errorContainer    = Color(0xFFFFDAD6),
    onErrorContainer  = Color(0xFF410002),
    background        = Color(0xFFF5F5F5),
    onBackground      = Color(0xFF1A1A1A),
    surface           = Color.White,
    onSurface         = Color(0xFF1A1A1A),
    surfaceVariant    = Color(0xFFE8EEF4),
    onSurfaceVariant  = Color(0xFF333333),
    outline           = Color(0xFFB0B0B0),
    outlineVariant    = Color(0xFFE0E0E0),
    scrim             = Color.Black,
    inverseSurface    = Color(0xFF1A1A1A),
    inverseOnSurface  = Color.White,
    inversePrimary    = AtilfazBlueAccent,
    surfaceTint       = AtilfazBlue
)

// Keep old name for backward compat
@Composable
fun StreamVaultTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = AtilfazTheme(darkTheme = darkTheme, content = content)

@Composable
fun AtilfazTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AtilfazDarkColors else AtilfazLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AtilfazBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
