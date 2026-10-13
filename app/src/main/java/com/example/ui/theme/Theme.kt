package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ShopNavyLight,
    onPrimary = Color.White,
    primaryContainer = ShopNavyDark,
    onPrimaryContainer = Color.White,
    secondary = ShopAmberLight,
    onSecondary = Color.Black,
    secondaryContainer = ShopAmber,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ShopNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = ShopNavyDark,
    secondary = ShopAmber,
    onSecondary = Color.White,
    secondaryContainer = ShopAmberContainer,
    onSecondaryContainer = Color(0xFF78350F),
    surface = ShopSurface,
    onSurface = ShopTextPrimary,
    background = ShopBackground,
    onBackground = ShopTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent high-contrast shopkeeper brand colors
    content: @Composable () -> Unit
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
