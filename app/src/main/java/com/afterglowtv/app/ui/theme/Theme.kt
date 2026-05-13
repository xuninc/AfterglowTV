package com.afterglowtv.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppShapes
import com.afterglowtv.app.ui.design.LocalAppShapes
import com.afterglowtv.app.ui.design.LocalAppSpacing
import com.afterglowtv.app.ui.design.LocalSafeArea
import com.afterglowtv.app.ui.design.rememberAppTypography
import com.afterglowtv.app.ui.design.resolveSafeArea

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.TiviAccent,
    onPrimary = OnPrimary,
    primaryContainer = AppColors.TiviAccentLight,
    onPrimaryContainer = AppColors.TiviSurfaceDeep,
    secondary = AppColors.TiviAccentLight,
    surface = AppColors.TiviSurfaceBase,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.TiviSurfaceCool,
    onSurfaceVariant = AppColors.TextSecondary,
    background = AppColors.TiviSurfaceDeep,
    onBackground = AppColors.TextPrimary,
    error = AppColors.EpgNowLine,
    onError = OnPrimary
)

@Composable
fun AfterglowTVTheme(content: @Composable () -> Unit) {
    val typography = rememberAppTypography()
    CompositionLocalProvider(
        LocalAppSpacing provides com.afterglowtv.app.ui.design.AppSpacing(),
        LocalAppShapes provides AppShapes(),
        LocalSafeArea provides resolveSafeArea()
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = typography,
            content = content
        )
    }
}
