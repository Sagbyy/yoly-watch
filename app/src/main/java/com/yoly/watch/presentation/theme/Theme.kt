package com.yoly.watch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val YolyColors = Colors(
    primary = Color.White,
    primaryVariant = Color(0xFFE0E0E0),
    secondary = Color.White,
    secondaryVariant = Color(0xFFBDBDBD),
    background = Color.Black,
    surface = Color(0xFF1C1C1C),
    error = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    onError = Color.Black,
)

@Composable
fun YolywatchTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = YolyColors,
        content = content,
    )
}
