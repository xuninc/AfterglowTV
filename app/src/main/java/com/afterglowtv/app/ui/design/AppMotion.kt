package com.afterglowtv.app.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

object AppMotion {
    const val Fast = 120
    const val Standard = 220                // TiViMate "u" base — was 160 ms
    const val Emphasis = 275                // TiViMate panel slide

    val FocusSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 150,               // TiViMate fast fade
        easing = FastOutSlowInEasing
    )

    // TiViMate v5.2.0 ground-truth motion durations / specs.
    val FocusFade: FiniteAnimationSpec<Float> = tween(150, easing = FastOutSlowInEasing)
    val Standard220: FiniteAnimationSpec<Float> = tween(220, easing = FastOutSlowInEasing)
    val PanelSlide: FiniteAnimationSpec<Float> = tween(275, easing = FastOutSlowInEasing)
    const val OsdAutoHideMs: Long = 3_000L
}
