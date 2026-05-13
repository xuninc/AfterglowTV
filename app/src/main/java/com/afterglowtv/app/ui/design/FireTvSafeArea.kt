package com.afterglowtv.app.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Amazon's TV-app guideline is to keep all UI inside the inner 90% of the screen.
 * That works out to 48 dp horizontal / 27 dp vertical at 1920x1080 / xhdpi.
 *
 * Android TV launchers already account for this in the launcher chrome, so we
 * leave it untouched there. The Fire TV launcher does not, and on cheaper
 * sticks the panel bleeds visibly into overscan, so we apply the padding only
 * when running on Fire OS (detected via the runtime feature flag).
 */
val LocalSafeArea = compositionLocalOf { PaddingValues(0.dp) }

fun fireTvSafeAreaPadding(): PaddingValues =
    PaddingValues(start = 48.dp, top = 27.dp, end = 48.dp, bottom = 27.dp)

fun androidTvSafeAreaPadding(): PaddingValues = PaddingValues(0.dp)

@Composable
@ReadOnlyComposable
fun resolveSafeArea(): PaddingValues {
    val ctx = LocalContext.current
    val isFireTv = ctx.packageManager.hasSystemFeature("amazon.hardware.fire_tv")
    return if (isFireTv) fireTvSafeAreaPadding() else androidTvSafeAreaPadding()
}
