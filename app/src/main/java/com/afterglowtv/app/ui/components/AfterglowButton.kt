package com.afterglowtv.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppShapeSet
import com.afterglowtv.app.ui.design.AppStyles
import com.afterglowtv.app.ui.design.GlowSpec
import com.afterglowtv.app.ui.design.afterglow

/**
 * Themed button. The actual silhouette is picked at composition time from
 * `AppStyles.value.button` — so when the user switches shape sets, every
 * AfterglowButton repaints into the new style instantly.
 *
 * Use for primary actions like Add Playlist, Save, Watch, Login.
 */
@Composable
fun AfterglowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val style = AppStyles.value.button
    val baseModifier = modifier
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 12.dp)

    when (style) {
        AppShapeSet.ButtonStyle.PILL -> SolidLabel(text, baseModifier, RoundedCornerShape(999.dp))
        AppShapeSet.ButtonStyle.SHARP -> SolidLabel(text, baseModifier, RoundedCornerShape(0.dp))
        AppShapeSet.ButtonStyle.SOFT -> SolidLabel(text, baseModifier, RoundedCornerShape(6.dp))
        AppShapeSet.ButtonStyle.CUT_CORNER -> SolidLabel(text, baseModifier, CutCornerShape(12.dp.value))
        AppShapeSet.ButtonStyle.STRIPE -> StripeLabel(text, baseModifier)
        AppShapeSet.ButtonStyle.GHOST -> GhostLabel(text, baseModifier)
        AppShapeSet.ButtonStyle.GLASS -> GlassLabel(text, baseModifier)
        AppShapeSet.ButtonStyle.DOUBLE_SHADOW -> DoubleShadowLabel(text, baseModifier)
    }
}

@Composable
private fun SolidLabel(text: String, mod: Modifier, shape: Shape) {
    Row(
        modifier = mod
            .clip(shape)
            .background(AppColors.TiviAccent),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = AppColors.TiviSurfaceDeep,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun StripeLabel(text: String, mod: Modifier) {
    Row(
        modifier = mod
            .clip(RoundedCornerShape(2.dp))
            .background(AppColors.TiviSurfaceCool)
            .border(
                BorderStroke(0.dp, Color.Transparent),
                shape = RoundedCornerShape(2.dp),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Accent stripe at leading edge
        Row(
            modifier = Modifier
                .padding(end = 12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.TiviAccent)
                .padding(horizontal = 2.dp, vertical = 10.dp),
        ) {}
        Text(
            text = text,
            color = AppColors.TextPrimary,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun GhostLabel(text: String, mod: Modifier) {
    Row(
        modifier = mod
            .border(1.5.dp, AppColors.TiviAccent, RoundedCornerShape(2.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = AppColors.TiviAccent,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun GlassLabel(text: String, mod: Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = mod
            .afterglow(
                listOf(GlowSpec(AppColors.TiviAccent, 14.dp, 0.35f)),
                shape,
            )
            .clip(shape)
            .background(AppColors.TiviAccent.copy(alpha = 0.10f))
            .border(1.dp, AppColors.TiviAccent.copy(alpha = 0.35f), shape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = AppColors.TiviAccentLight,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun DoubleShadowLabel(text: String, mod: Modifier) {
    val shape = RoundedCornerShape(2.dp)
    Row(
        modifier = mod
            .shadow(elevation = 8.dp, shape = shape, ambientColor = AppColors.EpgNowLine, spotColor = AppColors.EpgNowLine)
            .clip(shape)
            .background(AppColors.TiviAccent),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = AppColors.TiviSurfaceDeep,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

/** Cut-corner clip-path shape — top-left and bottom-right corners clipped at [cut] px. */
private fun CutCornerShape(cut: Float): Shape = GenericShape { size, _ ->
    moveTo(cut, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - cut)
    lineTo(size.width - cut, size.height)
    lineTo(0f, size.height)
    lineTo(0f, cut)
    close()
}
