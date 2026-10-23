package com.example.lab4

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Temă minimalistă: fundal aproape alb, text închis, o singură culoare de accent (indigo).
 * Nu folosim culorile dinamice (Material You), ca aspectul să fie același pe orice telefon.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF3949AB),
    onPrimary = Color.White,
    secondaryContainer = Color(0xFFE8EAF6),
    onSecondaryContainer = Color(0xFF3949AB),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1C),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1A1C),
    surfaceVariant = Color(0xFFF0F0F3),
    onSurfaceVariant = Color(0xFF6B6B74),
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFF0F0F3),
    outlineVariant = Color(0xFFE6E6EA),
    error = Color(0xFFD64545)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    onPrimary = Color(0xFF14152B),
    secondaryContainer = Color(0xFF2A2D4A),
    onSecondaryContainer = Color(0xFFC5CAE9),
    background = Color(0xFF121214),
    onBackground = Color(0xFFEDEDF0),
    surface = Color(0xFF121214),
    onSurface = Color(0xFFEDEDF0),
    surfaceVariant = Color(0xFF26262A),
    onSurfaceVariant = Color(0xFFA0A0AA),
    surfaceContainerLowest = Color(0xFF1B1B1E),
    surfaceContainerHigh = Color(0xFF222226),
    surfaceContainerHighest = Color(0xFF2B2B30),
    outlineVariant = Color(0xFF34343A),
    error = Color(0xFFEF7B7B)
)

@Composable
fun ToDoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
