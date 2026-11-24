package com.wordnerd.dictionary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

// Fallback palette (teal + coral, after the book cover) for devices without Material You.
private val Light = lightColorScheme(
    primary = Color(0xFF006A6A), onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF1F0), onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF4A6363), secondaryContainer = Color(0xFFCCE8E7),
    onSecondaryContainer = Color(0xFF051F1F),
    tertiary = Color(0xFF9C4234), tertiaryContainer = Color(0xFFFFDAD4),
    onTertiaryContainer = Color(0xFF3E0500),
    background = Color(0xFFF4FBFA), surface = Color(0xFFF4FBFA),
    surfaceContainer = Color(0xFFE9EFEE), surfaceContainerHigh = Color(0xFFE3E9E8),
    surfaceContainerLow = Color(0xFFEFF5F4),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF80D5D4), onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F), onPrimaryContainer = Color(0xFF9CF1F0),
    secondary = Color(0xFFB0CCCB), secondaryContainer = Color(0xFF324B4B),
    onSecondaryContainer = Color(0xFFCCE8E7),
    tertiary = Color(0xFFFFB4A7), tertiaryContainer = Color(0xFF7D2B1F),
    onTertiaryContainer = Color(0xFFFFDAD4),
    background = Color(0xFF0E1514), surface = Color(0xFF0E1514),
    surfaceContainer = Color(0xFF1A2121), surfaceContainerHigh = Color(0xFF242B2B),
    surfaceContainerLow = Color(0xFF161D1D),
)

/** Serif face used for headwords, for a classic dictionary feel. */
val HeadwordFont = FontFamily.Serif

@Composable
fun WordNerdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}
