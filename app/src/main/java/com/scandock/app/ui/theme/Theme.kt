package com.scandock.app.ui.theme

import android.app.Activity
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

// ---- ScanDock Brand Colors (NEW) ----

// Dark surfaces matching your icon background
val ScanDockDark = Color(0xFF0F0F10)
val ScanDockDarkSurface = Color(0xFF1A1B1E)

// Cyan scan beam colors
val ScanBeamBlue = Color(0xFF00C6FF)
val ScanBeamBlueDark = Color(0xFF008BCE)
val ScanGlow = Color(0xFF4DE1FF)

// Whites for paper + surfaces
val ScanDockWhite = Color(0xFFFFFFFF)
val ScanDockOffWhite = Color(0xFFF5F6F8)

// ---- Material Color Schemes ----

private val DarkColorScheme = darkColorScheme(
    primary = ScanBeamBlue,          // main accent (cyan)
    secondary = ScanGlow,            // glow
    tertiary = ScanBeamBlueDark,     // deeper cyan
    background = ScanDockDark,
    surface = ScanDockDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = ScanDockWhite,
    onSurface = ScanDockWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ScanBeamBlue,
    secondary = ScanGlow,
    tertiary = ScanBeamBlueDark,
    background = ScanDockOffWhite,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ScanDockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
