package com.afterglowtv.app.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One layer of glow. Stack multiple to get layered haloes (subtle close
 * halo + dramatic outer bloom + slightly-offset accent at the bottom etc.).
 *
 * Implemented on top of Compose's `Modifier.shadow(ambientColor, spotColor)`,
 * so the cost is a single shadow pass per layer — no Canvas / RenderScript.
 * The trade-off is that all glows have a slight downward bias inherent to
 * the spot-light shadow model; for a fully-isotropic glow, layer a
 * `offsetY = -radius/2` spec to cancel the bias.
 */
data class GlowSpec(
    val color: Color,
    /** Total glow extent (the spec's radius == shadow elevation). */
    val radius: Dp,
    /** Final alpha multiplier applied on top of `color.alpha`. 0f hides this layer. */
    val opacity: Float = 1f,
) {
    companion object {
        val None = GlowSpec(Color.Transparent, 0.dp, 0f)
    }
}

/**
 * Author-curated glow presets — each maps to a *role* in the UI.
 * The Settings screen lets users override any of these without code changes.
 */
object Glows {
    /** Used by `tivimateFocus(role = Card | Pill)` when the user's focus style is GLOW. */
    var focus: List<GlowSpec> by mutableStateOf(
        listOf(
            GlowSpec(color = AppColors.TiviAccent, radius = 12.dp, opacity = 0.55f),
            GlowSpec(color = AppColors.TiviAccent, radius = 28.dp, opacity = 0.28f),
        )
    )
        private set

    /** Live indicator pulse, recording dot, EPG now-line halo. */
    var live: List<GlowSpec> by mutableStateOf(
        listOf(
            GlowSpec(color = AppColors.EpgNowLine, radius = 10.dp, opacity = 0.85f),
            GlowSpec(color = AppColors.EpgNowLine, radius = 24.dp, opacity = 0.35f),
        )
    )
        private set

    /** Cards / posters / hero blocks ambient glow. */
    var ambient: List<GlowSpec> by mutableStateOf(
        listOf(
            GlowSpec(color = AppColors.TiviAccentMuted, radius = 18.dp, opacity = 0.25f),
        )
    )
        private set

    /** Global intensity multiplier applied on top of every layer. 0f disables all glows. */
    var intensity: Float by mutableStateOf(1f)
        private set

    fun applyIntensity(value: Float) { intensity = value.coerceIn(0f, 2f) }
    fun overrideFocus(specs: List<GlowSpec>) { focus = specs }
    fun overrideLive(specs: List<GlowSpec>) { live = specs }
    fun overrideAmbient(specs: List<GlowSpec>) { ambient = specs }

    /** Scale the configured specs by the current [intensity] before painting. */
    internal fun scaled(specs: List<GlowSpec>): List<GlowSpec> {
        val k = intensity
        if (k == 0f) return emptyList()
        if (k == 1f) return specs
        return specs.map { it.copy(opacity = (it.opacity * k).coerceIn(0f, 1f)) }
    }
}

/**
 * Paint one or more glow layers behind an element. Apply this **before**
 * any `background()` or `clip()` modifiers so the glow renders outside the
 * element's own surface.
 *
 *     Box(
 *         Modifier
 *             .afterglow(Glows.focus, RoundedCornerShape(4.dp))   // glow first
 *             .background(palette.surfaceBase)                     // surface on top
 *             ...
 *     )
 */
fun Modifier.afterglow(
    specs: List<GlowSpec>,
    shape: Shape = RectangleShape,
): Modifier {
    val scaled = Glows.scaled(specs)
    if (scaled.isEmpty()) return this
    var m: Modifier = this
    for (spec in scaled) {
        if (spec.color == Color.Transparent || spec.radius.value == 0f || spec.opacity == 0f) continue
        val color = spec.color.copy(alpha = (spec.color.alpha * spec.opacity).coerceIn(0f, 1f))
        m = m.shadow(
            elevation = spec.radius,
            shape = shape,
            clip = false,
            ambientColor = color,
            spotColor = color,
        )
    }
    return m
}

/** Single-spec convenience overload. */
fun Modifier.afterglow(spec: GlowSpec, shape: Shape = RectangleShape): Modifier =
    afterglow(listOf(spec), shape)
