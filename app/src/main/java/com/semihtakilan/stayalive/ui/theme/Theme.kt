package com.semihtakilan.stayalive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.semihtakilan.stayalive.data.preferences.UserPreferences

private val MedicalBlueDark = Color(0xFF0A84FF)
private val MedicalBlueLight = Color(0xFF007AFF)
private val MedicalTeal = Color(0xFF30B0C7)

private val DarkColorScheme = darkColorScheme(
    primary = MedicalBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C2E45),
    onPrimaryContainer = Color(0xFFB3D7FF),
    secondary = MedicalTeal,
    onSecondary = Color(0xFF001F24),
    tertiary = Color(0xFF64D2FF),
    onTertiary = Color(0xFF001F2A),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF48484A),
    outlineVariant = Color(0xFF3A3A3C),
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF002F5E),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    tertiary = Color(0xFF5AC8FA),
    onTertiary = Color(0xFF001A26),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF636366),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE0E0E5),
)

@Composable
fun StayAliveTheme(
    themePref: String = UserPreferences.PREF_THEME_SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themePref) {
        UserPreferences.PREF_THEME_DARK -> true
        UserPreferences.PREF_THEME_LIGHT -> false
        else -> darkTheme
    }
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
