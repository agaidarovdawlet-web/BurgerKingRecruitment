package com.example.bkrecruitment.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BkColorScheme = lightColorScheme(
    primary = AppColors.Highlight,
    onPrimary = AppColors.HighlightText,
    background = AppColors.Background,
    surface = AppColors.Surface,
    onSurface = AppColors.PrimaryText,
    secondary = AppColors.TopBar,
    onSecondary = AppColors.TopBarText,
)

@Composable
fun BkHiringTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BkColorScheme,
        typography = BkTypography,
        content = content,
    )
}
