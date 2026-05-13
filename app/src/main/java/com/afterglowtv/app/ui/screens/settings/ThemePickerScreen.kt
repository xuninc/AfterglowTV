package com.afterglowtv.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.R
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppPalette
import com.afterglowtv.app.ui.design.GlowSpec
import com.afterglowtv.app.ui.design.afterglow
import com.afterglowtv.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemePickerViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
) : ViewModel() {
    val activePaletteId = preferences.themePalette.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "vaporwave",
    )

    fun select(palette: AppPalette) {
        viewModelScope.launch {
            preferences.setThemePalette(palette.id)
            AppColors.applyPalette(palette)
        }
    }
}

@Composable
fun ThemePickerScreen(
    onBack: () -> Unit,
    viewModel: ThemePickerViewModel = hiltViewModel(),
) {
    val activeId by viewModel.activePaletteId.collectAsState()
    val palettes = AppPalette.ALL

    Box(modifier = Modifier.fillMaxSize()) {
        // Layered backdrop — vertical gradient + warm/cool radial melts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.TiviSurfaceDeep,
                            AppColors.TiviSurfaceBase,
                            AppColors.TiviSurfaceCool,
                        ),
                    )
                )
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(
                        AppColors.TiviAccent.copy(alpha = 0.28f),
                        AppColors.TiviAccent.copy(alpha = 0f),
                    ),
                    center = Offset(2400f, -200f),
                    radius = 1400f,
                )
            )
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(
                        AppColors.EpgNowLine.copy(alpha = 0.22f),
                        AppColors.EpgNowLine.copy(alpha = 0f),
                    ),
                    center = Offset(300f, 1900f),
                    radius = 1100f,
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
        ) {
            // ── Hero brand strip ──────────────────────────────────────────
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.afterglow_logo),
                    contentDescription = "Afterglow TV",
                    modifier = Modifier
                        .size(56.dp)
                        .afterglow(
                            specs = listOf(
                                GlowSpec(AppColors.TiviAccent, 16.dp, 0.55f),
                                GlowSpec(AppColors.EpgNowLine, 28.dp, 0.30f),
                            ),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clip(RoundedCornerShape(14.dp)),
                )
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Themes",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = TextUnit(36f, TextUnitType.Sp),
                            ),
                            color = AppColors.TextPrimary,
                        )
                    }
                    Text(
                        text = "Pick a vibe. Everything reflows instantly — no restart, no fuss.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TiviAccentLight,
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                items(palettes, key = { it.id }) { palette ->
                    PaletteCard(
                        palette = palette,
                        isSelected = palette.id == activeId,
                        onSelect = { viewModel.select(palette) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteCard(
    palette: AppPalette,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (isSelected) base.afterglow(
                    specs = listOf(
                        GlowSpec(palette.accent, 16.dp, 0.7f),
                        GlowSpec(palette.accent, 32.dp, 0.35f),
                    ),
                    shape = shape,
                ) else base
            }
            .clip(shape)
            .background(palette.surfaceBase)
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) palette.accent else palette.surfaceAccent,
                ),
                shape,
            )
            .clickable { onSelect() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Larger swatch preview — feels more deliberate
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Swatch(palette.surfaceDeep, size = 40.dp)
            Swatch(palette.surfaceCool, size = 40.dp)
            Swatch(palette.accent, size = 52.dp, isFeatured = true)
            Swatch(palette.accentLight, size = 40.dp)
            Swatch(palette.nowLine, size = 40.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = palette.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                )
                if (isSelected) {
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.accent)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = TextUnit(1f, TextUnitType.Sp),
                            ),
                            color = palette.surfaceDeep,
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = palette.description,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Swatch(
    color: androidx.compose.ui.graphics.Color,
    size: androidx.compose.ui.unit.Dp,
    isFeatured: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(
                width = if (isFeatured) 2.dp else 1.dp,
                color = AppColors.Divider,
                shape = RoundedCornerShape(8.dp),
            ),
    )
}
