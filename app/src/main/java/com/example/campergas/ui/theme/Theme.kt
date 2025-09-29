package com.example.campergas.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.campergas.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = CamperBlueLight,
    secondary = CamperGreenLight,
    tertiary = CamperOrangeLight,
    error = CamperErrorLight,
    background = CamperBackgroundDark,
    surface = CamperSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onError = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    // Additional semantic colors for better UX
    primaryContainer = CamperBlueDark,
    onPrimaryContainer = Color.White,
    secondaryContainer = CamperGreenDark,
    onSecondaryContainer = Color.White,
    tertiaryContainer = CamperOrangeDark,
    onTertiaryContainer = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = CamperBlue,
    secondary = CamperGreen,
    tertiary = CamperOrange,
    error = CamperError,
    background = CamperBackgroundLight,
    surface = CamperSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onError = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    // Additional semantic colors for better UX
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = CamperBlueDark,
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = CamperGreenDark,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = CamperOrangeDark,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF5F5F5F)
)

@Composable
fun CamperGasTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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