package com.afterglowtv.app.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.R
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.GlowSpec
import com.afterglowtv.app.ui.design.afterglow
import com.afterglowtv.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _hasProviders = MutableStateFlow<Boolean?>(null)
    val hasProviders: StateFlow<Boolean?> = _hasProviders.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.getProviders()
                .map { it.isNotEmpty() }
                .collect { _hasProviders.value = it }
        }
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val hasProviders by viewModel.hasProviders.collectAsStateWithLifecycle()

    LaunchedEffect(hasProviders) {
        when (hasProviders) {
            true -> onNavigateToHome()
            false -> onNavigateToSetup()
            null -> Unit
        }
    }

    // ── Branded loading splash ───────────────────────────────────────────
    // This usually flashes for ~100 ms while the provider count loads.
    // Even at that duration it's worth making the first thing users see feel
    // like the product they downloaded.
    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical surface gradient + two radial accent melts (matching the
        // rest of the Afterglow brand language).
        Box(
            modifier = Modifier.fillMaxSize().background(
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
                        AppColors.TiviAccent.copy(alpha = 0.30f),
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
                    center = Offset(400f, 1900f),
                    radius = 1200f,
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo with stacked teal + porange glow halo
            Image(
                painter = painterResource(id = R.drawable.afterglow_logo),
                contentDescription = "Afterglow TV",
                modifier = Modifier
                    .size(120.dp)
                    .afterglow(
                        specs = listOf(
                            GlowSpec(AppColors.TiviAccent, 32.dp, 0.65f),
                            GlowSpec(AppColors.EpgNowLine, 56.dp, 0.40f),
                        ),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .clip(RoundedCornerShape(28.dp)),
            )

            Spacer(Modifier.height(24.dp))

            // "Afterglow TV" wordmark inline — same composition as provider hero
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Afterglow",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(56f, TextUnitType.Sp),
                    ),
                    color = AppColors.TextPrimary,
                )
                Spacer(Modifier.size(14.dp))
                Text(
                    text = "TV",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = TextUnit(48f, TextUnitType.Sp),
                    ),
                    color = AppColors.TiviAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .afterglow(
                            specs = listOf(GlowSpec(AppColors.TiviAccent, 12.dp, 0.55f)),
                        ),
                )
            }

            Spacer(Modifier.height(28.dp))

            CircularProgressIndicator(
                color = AppColors.TiviAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
