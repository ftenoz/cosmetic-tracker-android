package com.cosmetictracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VanityPrimary,
    onPrimary = VanityOnPrimary,
    primaryContainer = VanityPrimaryContainer,
    onPrimaryContainer = VanityOnPrimary,
    secondary = VanitySecondary,
    onSecondary = VanityOnPrimary,
    secondaryContainer = VanitySecondaryContainer,
    onSecondaryContainer = VanityOnSecondaryContainer,
    tertiary = VanityTertiary,
    onTertiary = VanityOnTertiary,
    error = VanityError,
    onError = VanityOnError,
    errorContainer = VanityError,
    onErrorContainer = VanityOnError,
    background = VanitySurface,
    onBackground = VanityOnSurface,
    surface = VanitySurface,
    onSurface = VanityOnSurface,
    surfaceVariant = VanitySurfaceVariant,
    onSurfaceVariant = VanityOnSurfaceVariant,
    outline = VanityOutlineVariant,
    outlineVariant = VanityOutlineVariant,
    surfaceContainer = VanitySurfaceContainer,
    surfaceContainerHighest = VanitySurfaceVariant,
    surfaceContainerLow = VanitySurfaceContainerLow,
    surfaceContainerLowest = VanitySurfaceContainerLowest
)

private val DarkColorScheme = darkColorScheme(
    primary = EtherealPrimary,
    onPrimary = EtherealOnPrimary,
    primaryContainer = EtherealPrimaryContainer,
    onPrimaryContainer = EtherealOnPrimary,
    secondary = EtherealSecondary,
    onSecondary = EtherealOnPrimary,
    secondaryContainer = EtherealSecondaryContainer,
    onSecondaryContainer = EtherealOnSecondaryContainer,
    tertiary = EtherealTertiary,
    onTertiary = EtherealOnTertiary,
    error = EtherealError,
    onError = EtherealOnError,
    errorContainer = EtherealError,
    onErrorContainer = EtherealOnError,
    background = EtherealSurface,
    onBackground = EtherealOnSurface,
    surface = EtherealSurface,
    onSurface = EtherealOnSurface,
    surfaceVariant = EtherealSurfaceVariant,
    onSurfaceVariant = EtherealOnSurfaceVariant,
    outline = EtherealOutlineVariant,
    outlineVariant = EtherealOutlineVariant,
    surfaceContainer = EtherealSurfaceContainer,
    surfaceContainerHighest = EtherealSurfaceVariant,
    surfaceContainerLow = EtherealSurfaceContainerLow,
    surfaceContainerLowest = EtherealSurfaceContainerLowest
)

@Composable
fun CosmeticTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
