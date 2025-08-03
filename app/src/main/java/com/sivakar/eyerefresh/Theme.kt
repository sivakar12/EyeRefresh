package com.sivakar.eyerefresh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F6918),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFF290),
    onPrimaryContainer = Color(0xFF0B2000),
    secondary = Color(0xFF3D691A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBDF292),
    onSecondaryContainer = Color(0xFF0B2000),
    tertiary = Color(0xFF3D691A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBDF292),
    onTertiaryContainer = Color(0xFF0B2000),
    error = Color(0xFFBA1B1B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410001),
    background = Color(0xFFFFFBFD),
    onBackground = Color(0xFF1D1B1F),
    surface = Color(0xFFFFFBFD),
    onSurface = Color(0xFF1D1B1F),
    surfaceVariant = Color(0xFFE6E0EC),
    onSurfaceVariant = Color(0xFF48454F),
    outline = Color(0xFF79767F),
    inverseOnSurface = Color(0xFFF4EFF4),
    inverseSurface = Color(0xFF323033),
    inversePrimary = Color(0xFFA4D577),
    surfaceTint = Color(0xFF3F6918),
    outlineVariant = Color(0xFFC9C4CF),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA4D577),
    onPrimary = Color(0xFF183800),
    primaryContainer = Color(0xFF295000),
    onPrimaryContainer = Color(0xFFBFF290),
    secondary = Color(0xFFA2D579),
    onSecondary = Color(0xFF163800),
    secondaryContainer = Color(0xFF275100),
    onSecondaryContainer = Color(0xFFBDF292),
    tertiary = Color(0xFFA2D579),
    onTertiary = Color(0xFF163800),
    tertiaryContainer = Color(0xFF275100),
    onTertiaryContainer = Color(0xFFBDF292),
    error = Color(0xFFFFB4A9),
    onError = Color(0xFF680003),
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD4),
    background = Color(0xFF1D1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF48454F),
    onSurfaceVariant = Color(0xFFC9C4CF),
    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF1D1B1F),
    inverseSurface = Color(0xFFE6E1E5),
    inversePrimary = Color(0xFF3F6918),
    surfaceTint = Color(0xFFA4D577),
    outlineVariant = Color(0xFF48454F),
    scrim = Color(0xFF000000),
)

@Composable
fun EyeRefreshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
) 