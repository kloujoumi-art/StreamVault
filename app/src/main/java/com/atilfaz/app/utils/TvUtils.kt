package com.atilfaz.app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsTV(): Boolean {
    val context = LocalContext.current
    return remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}

fun Modifier.tvFocusBorder(
    focusedColor: Color = Color(0xFF1E88E5),
    unfocusedColor: Color = Color.Transparent
): Modifier {
    var isFocused = false
    return this
        .onFocusChanged { isFocused = it.isFocused }
        .drawBehind {
            val color = if (isFocused) focusedColor else unfocusedColor
            if (color != Color.Transparent) {
                drawRoundRect(
                    color = color,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }
}

fun Modifier.handleDpadAction(onSelect: () -> Unit): Modifier =
    this.onKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp &&
            (event.key == Key.DirectionCenter || event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            onSelect()
            true
        } else false
    }
