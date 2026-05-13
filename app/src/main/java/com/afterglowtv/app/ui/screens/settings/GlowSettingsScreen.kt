package com.afterglowtv.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.GlowSpec
import com.afterglowtv.app.ui.design.Glows
import com.afterglowtv.app.ui.design.afterglow

/**
 * Glow customization. Live preview tile reacts to every slider tick — same
 * pattern as the YouTube tweak. Per-role: color (preset chips), radius
 * (sliders), opacity (sliders). Plus a master intensity multiplier that
 * scales everything together (set to 0 to disable all glows).
 */
@Composable
fun GlowSettingsScreen(onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.TiviSurfaceDeep)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = "Glow",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Color, radius, opacity, intensity — per role. Changes apply live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                )
            }

            item { MasterIntensityCard() }

            item {
                GlowRoleCard(
                    label = "Focus halo",
                    description = "The glow on focused rows, cards, and pills.",
                    specs = Glows.focus,
                    onChange = { Glows.overrideFocus(it) },
                )
            }
            item {
                GlowRoleCard(
                    label = "Live & now-line",
                    description = "Pulse around the LIVE pill, recording indicator, and EPG now-line.",
                    specs = Glows.live,
                    onChange = { Glows.overrideLive(it) },
                )
            }
            item {
                GlowRoleCard(
                    label = "Ambient",
                    description = "Subtle halo on cards, posters, and hero blocks.",
                    specs = Glows.ambient,
                    onChange = { Glows.overrideAmbient(it) },
                )
            }
        }
    }
}

@Composable
private fun MasterIntensityCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(AppColors.TiviSurfaceBase)
            .border(1.dp, AppColors.TiviSurfaceCool, RoundedCornerShape(4.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Master intensity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "%.2fx".format(Glows.intensity),
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TiviAccentLight,
            )
        }
        Text(
            text = "Multiplies every glow's opacity. 0 disables glow entirely; 2 doubles every halo.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextTertiary,
        )
        Slider(
            value = Glows.intensity,
            onValueChange = { Glows.applyIntensity(it) },
            valueRange = 0f..2f,
            colors = sliderColors(),
        )
    }
}

@Composable
private fun GlowRoleCard(
    label: String,
    description: String,
    specs: List<GlowSpec>,
    onChange: (List<GlowSpec>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(AppColors.TiviSurfaceBase)
            .border(1.dp, AppColors.TiviSurfaceCool, RoundedCornerShape(4.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextTertiary,
                )
            }
            GlowPreviewTile(specs)
        }

        specs.forEachIndexed { index, spec ->
            GlowLayerControls(
                layerIndex = index + 1,
                spec = spec,
                onChange = { updated ->
                    onChange(specs.toMutableList().also { it[index] = updated })
                },
            )
        }

        if (specs.size < 3) {
            Text(
                text = "+ Add layer",
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.TiviAccentMuted)
                    .clickable {
                        val next = specs + GlowSpec(AppColors.TiviAccent, 16.dp, 0.35f)
                        onChange(next)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TiviAccentLight,
            )
        }
    }
}

@Composable
private fun GlowPreviewTile(specs: List<GlowSpec>) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(96.dp, 56.dp)
            .afterglow(specs, shape)
            .clip(shape)
            .background(AppColors.TiviSurfaceCool)
            .border(1.dp, AppColors.TiviAccent.copy(alpha = 0.5f), shape),
    )
}

@Composable
private fun GlowLayerControls(
    layerIndex: Int,
    spec: GlowSpec,
    onChange: (GlowSpec) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(AppColors.TiviSurfaceCool.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Layer $layerIndex",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.TiviAccentLight,
                modifier = Modifier.weight(1f),
            )
            ColorSwatches(
                selected = spec.color,
                onPick = { onChange(spec.copy(color = it)) },
            )
        }

        // Radius slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Radius",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                modifier = Modifier.width(72.dp),
            )
            Slider(
                value = spec.radius.value,
                onValueChange = { onChange(spec.copy(radius = it.dp)) },
                valueRange = 0f..48f,
                colors = sliderColors(),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "%.0f dp".format(spec.radius.value),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TiviAccentLight,
                modifier = Modifier.width(56.dp),
            )
        }

        // Opacity slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Opacity",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
                modifier = Modifier.width(72.dp),
            )
            Slider(
                value = spec.opacity,
                onValueChange = { onChange(spec.copy(opacity = it)) },
                valueRange = 0f..1f,
                colors = sliderColors(),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "%.0f%%".format(spec.opacity * 100f),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TiviAccentLight,
                modifier = Modifier.width(56.dp),
            )
        }
    }
}

@Composable
private fun ColorSwatches(selected: Color, onPick: (Color) -> Unit) {
    val palette = listOf(
        AppColors.TiviAccent,
        AppColors.TiviAccentLight,
        AppColors.EpgNowLine,
        AppColors.Live,
        AppColors.Warning,
        AppColors.Info,
        Color(0xFFFFFFFF),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        palette.forEach { c ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(
                        width = if (c == selected) 2.dp else 0.5.dp,
                        color = if (c == selected) AppColors.TextPrimary else AppColors.TextTertiary,
                        shape = CircleShape,
                    )
                    .clickable { onPick(c) },
            )
        }
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = AppColors.TiviAccent,
    activeTrackColor = AppColors.TiviAccent,
    inactiveTrackColor = AppColors.TiviSurfaceAccent,
)
