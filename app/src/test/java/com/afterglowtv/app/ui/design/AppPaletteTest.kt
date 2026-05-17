package com.afterglowtv.app.ui.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppPaletteTest {

    @Test
    fun `afterglow light palettes avoid whiteout brightness`() {
        val lightPalettes = listOf(
            AppPalette.AfterglowLight1,
            AppPalette.AfterglowLight2,
            AppPalette.AfterglowLight3,
            AppPalette.AfterglowLight4,
            AppPalette.AfterglowGrayLight,
            AppPalette.RachelsSunsetLight
        )

        lightPalettes.forEach { palette ->
            assertThat(palette.surfaceDeep.luminance()).isLessThan(0.82f)
            assertThat(contrastRatio(palette.textPrimary, palette.surfaceCool)).isAtLeast(4.5f)
        }
    }

    @Test
    fun `rachels sunset keeps mint teal as the dominant accent`() {
        listOf(AppPalette.SunsetAurora, AppPalette.RachelsSunsetLight).forEach { palette ->
            assertThat(palette.accent.green).isGreaterThan(palette.accent.red)
            assertThat(palette.accent.green).isGreaterThan(palette.accent.blue)
            assertThat(palette.success).isEqualTo(palette.accent)
        }
    }

    @Test
    fun `violet spectrum stays violet instead of becoming a blue theme`() {
        val palette = AppPalette.UltravioletSpectrum

        assertThat(palette.description.lowercase()).doesNotContain("blue")
        assertThat(palette.accent).isEqualTo(Color(0xFF7D64DC))
        assertThat(palette.live).isEqualTo(Color(0xFF5C36E2))
    }

    private fun contrastRatio(a: Color, b: Color): Float {
        val lighter = maxOf(a.luminance(), b.luminance())
        val darker = minOf(a.luminance(), b.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
