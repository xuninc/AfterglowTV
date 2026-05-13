package com.afterglowtv.app.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.R
import com.afterglowtv.app.ui.theme.OnSurface
import com.afterglowtv.app.ui.theme.OnSurfaceDim
import com.afterglowtv.app.ui.theme.Primary

@Composable
internal fun SyncingOverlay(
    isSyncing: Boolean,
    providerName: String? = null,
    progress: String? = null
) {
    if (!isSyncing) return

    BackHandler(enabled = true) {}

    val overlayFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { overlayFocusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = true, onClick = {})
            .focusRequester(overlayFocusRequester)
            .focusable()
            .onKeyEvent { true },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Primary)
            Text(
                text = stringResource(R.string.settings_syncing_title),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
            Text(
                text = providerName ?: stringResource(R.string.settings_syncing_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            progress?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}