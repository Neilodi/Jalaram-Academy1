package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF67DBB2), // Soft forest moss/teal
    secondary = JalaramPrimaryLight, // Sage Green Accent
    tertiary = JalaramSuccessContainer, // Soft Grass Green
    background = Color(0xFF111410), // Warm Wood Charcoal
    surface = Color(0xFF191D17), // Deep Leaf Spruce Grey
    onPrimary = Color(0xFF00382E),
    onSecondary = Color(0xFF1A330E),
    onBackground = Color(0xFFE2E3D8),
    onSurface = Color(0xFFE2E3D8)
)

private val LightColorScheme = lightColorScheme(
    primary = JalaramPrimary,
    secondary = JalaramAccent,
    tertiary = JalaramSuccess,
    background = JalaramBgMain,
    surface = JalaramSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = JalaramTextMain,
    onSurface = JalaramTextMain,
    outlineVariant = JalaramBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use hardcoded school brand colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
