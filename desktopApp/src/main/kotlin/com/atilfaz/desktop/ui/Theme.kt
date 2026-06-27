package com.atilfaz.desktop.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val BgColor        = Color(0xFF000000)
val SurfaceColor   = Color(0xFF0D0D0D)
val CardColor      = Color(0xFF141414)
val BorderColor    = Color(0xFF2A2A2A)
val BlueMain       = Color(0xFF1565C0)
val BlueLight      = Color(0xFF1E88E5)
val TextPrimary    = Color(0xFFFFFFFF)
val TextSecondary  = Color(0xFFB0B0B0)
val TextHint       = Color(0xFF606060)
val LiveRed        = Color(0xFFF44336)
val GoldColor      = Color(0xFFFFC107)

val AppColorScheme = darkColorScheme(
    primary = BlueMain,
    onPrimary = Color.White,
    secondary = BlueLight,
    onSecondary = Color.White,
    background = BgColor,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = CardColor,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)
