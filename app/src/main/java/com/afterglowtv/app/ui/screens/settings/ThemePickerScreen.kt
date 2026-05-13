package com.afterglowtv.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppPalette
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
        "neon_dusk",
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.TiviSurfaceDeep),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
        ) {
            Text(
                text = "Themes",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pick a palette. The whole app re-skins instantly — no restart needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
            )
            Spacer(Modifier.height(24.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val borderColor = if (isSelected) AppColors.TiviAccent else AppColors.TiviSurfaceCool
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceBase)
            .border(BorderStroke(if (isSelected) 3.dp else 1.dp, borderColor), RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Color swatch preview
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Swatch(palette.surfaceDeep, size = 36.dp)
            Swatch(palette.surfaceCool, size = 36.dp)
            Swatch(palette.accent, size = 44.dp)
            Swatch(palette.accentLight, size = 36.dp)
            Swatch(palette.nowLine, size = 36.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = palette.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                )
                if (isSelected) {
                    Spacer(Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(palette.accentMuted)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.textPrimary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
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
private fun Swatch(color: androidx.compose.ui.graphics.Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(6.dp)),
    )
}
