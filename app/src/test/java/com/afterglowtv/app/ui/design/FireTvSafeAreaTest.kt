package com.afterglowtv.app.ui.design

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FireTvSafeAreaTest {

    @Test
    fun `fire tv padding is 48dp horizontal and 27dp vertical`() {
        val p = fireTvSafeAreaPadding()
        assertThat(p.calculateLeftPadding(LayoutDirection.Ltr)).isEqualTo(48.dp)
        assertThat(p.calculateRightPadding(LayoutDirection.Ltr)).isEqualTo(48.dp)
        assertThat(p.calculateTopPadding()).isEqualTo(27.dp)
        assertThat(p.calculateBottomPadding()).isEqualTo(27.dp)
    }

    @Test
    fun `android tv padding is zero`() {
        val p = androidTvSafeAreaPadding()
        assertThat(p.calculateTopPadding()).isEqualTo(0.dp)
        assertThat(p.calculateBottomPadding()).isEqualTo(0.dp)
        assertThat(p.calculateLeftPadding(LayoutDirection.Ltr)).isEqualTo(0.dp)
        assertThat(p.calculateRightPadding(LayoutDirection.Ltr)).isEqualTo(0.dp)
    }
}
