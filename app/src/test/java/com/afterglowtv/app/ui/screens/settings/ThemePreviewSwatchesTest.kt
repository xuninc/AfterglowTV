package com.afterglowtv.app.ui.screens.settings

import com.afterglowtv.app.ui.design.AppPalette
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreviewSwatchesTest {

    @Test
    fun `afterglow light previews expose actual accents instead of only pale surfaces`() {
        val lightPalettes = listOf(
            AppPalette.AfterglowLight1,
            AppPalette.AfterglowLight2,
            AppPalette.AfterglowLight3,
            AppPalette.AfterglowLight4,
            AppPalette.RachelsSunsetLight,
        )

        lightPalettes.forEach { palette ->
            val swatches = themePreviewSwatches(palette)

            assertThat(swatches).contains(palette.accent)
            assertThat(swatches).contains(palette.accentLight)
            assertThat(swatches).contains(palette.nowLine)
            assertThat(swatches.toSet().size).isEqualTo(swatches.size)
        }
    }

    @Test
    fun `afterglow dark previews expose actual accents instead of only matching dark surfaces`() {
        val darkPalettes = listOf(
            AppPalette.Afterglow1,
            AppPalette.AfterglowSunset,
            AppPalette.Afterglow4,
            AppPalette.AfterglowGray,
        )

        darkPalettes.forEach { palette ->
            val swatches = themePreviewSwatches(palette)

            assertThat(swatches).contains(palette.accent)
            assertThat(swatches).contains(palette.accentLight)
            assertThat(swatches).contains(palette.nowLine)
            assertThat(swatches.toSet().size).isEqualTo(swatches.size)
        }
    }

    @Test
    fun `preview swatches are ordered by usable UI roles`() {
        val palette = AppPalette.UltravioletSpectrum

        assertEquals(
            listOf(
                palette.surfaceDeep,
                palette.surfaceBase,
                palette.accent,
                palette.accentLight,
                palette.nowLine,
                palette.live,
            ),
            themePreviewSwatches(palette),
        )
    }
}
