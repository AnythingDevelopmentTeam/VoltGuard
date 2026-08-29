package com.example.voltguard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private data class AccentColors(
    val light: ColorSchemeColors,
    val dark: ColorSchemeColors
)

private data class ColorSchemeColors(
    val primary: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val tertiary: androidx.compose.ui.graphics.Color
)

private val accentPalettes = mapOf(
    "green" to AccentColors(
        light = ColorSchemeColors(VoltGreen40, VoltGreenGrey40, VoltTeal40),
        dark = ColorSchemeColors(VoltGreen80, VoltGreenGrey80, VoltTeal80)
    ),
    "blue" to AccentColors(
        light = ColorSchemeColors(VoltBlue40, VoltBlueGrey40, VoltTeal40),
        dark = ColorSchemeColors(VoltBlue80, VoltBlueGrey80, VoltTeal80)
    ),
    "teal" to AccentColors(
        light = ColorSchemeColors(VoltTeal40, VoltGreenGrey40, VoltBlue40),
        dark = ColorSchemeColors(VoltTeal80, VoltGreenGrey80, VoltBlue80)
    ),
    "purple" to AccentColors(
        light = ColorSchemeColors(VoltPurple40, VoltPurpleGrey40, VoltTeal40),
        dark = ColorSchemeColors(VoltPurple80, VoltPurpleGrey80, VoltTeal80)
    ),
    "orange" to AccentColors(
        light = ColorSchemeColors(VoltOrange40, VoltOrangeGrey40, VoltTeal40),
        dark = ColorSchemeColors(VoltOrange80, VoltOrangeGrey80, VoltTeal80)
    ),
    "red" to AccentColors(
        light = ColorSchemeColors(VoltRed40, VoltRedGrey40, VoltTeal40),
        dark = ColorSchemeColors(VoltRed80, VoltRedGrey80, VoltTeal80)
    )
)

@Composable
fun VoltGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accent: String = "green",
    content: @Composable () -> Unit
) {
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = accentPalettes[accent] ?: accentPalettes.getValue("green")

    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = colors.dark.primary,
            secondary = colors.dark.secondary,
            tertiary = colors.dark.tertiary
        )

        else -> lightColorScheme(
            primary = colors.light.primary,
            secondary = colors.light.secondary,
            tertiary = colors.light.tertiary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}