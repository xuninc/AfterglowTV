package com.afterglowtv.app.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/**
 * Tiny text-based serializer for `List<GlowSpec>` so the user's per-role
 * glow customizations persist across app restarts.
 *
 * Format:
 *   "AARRGGBB|radiusDp|opacity;AARRGGBB|radiusDp|opacity"
 *
 * Empty string → use the in-code defaults defined on the [Glows] registry.
 */
object GlowSerialization {

    fun serialize(specs: List<GlowSpec>): String =
        specs.joinToString(";") { spec ->
            val argb = spec.color.toArgb().toLong() and 0xFFFFFFFFL
            val hex = String.format("%08X", argb)
            "$hex|${spec.radius.value}|${spec.opacity}"
        }

    fun deserialize(text: String): List<GlowSpec>? {
        if (text.isBlank()) return null
        return runCatching {
            text.split(";").map { part ->
                val parts = part.split("|")
                require(parts.size == 3) { "expected 3 fields, got ${parts.size}" }
                val color = Color(parts[0].toLong(16).toInt())
                val radius = parts[1].toFloat().dp
                val opacity = parts[2].toFloat()
                GlowSpec(color = color, radius = radius, opacity = opacity)
            }
        }.getOrNull()
    }
}
