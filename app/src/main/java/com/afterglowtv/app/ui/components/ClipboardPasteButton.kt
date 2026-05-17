package com.afterglowtv.app.ui.components

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.FocusSpec
import com.afterglowtv.app.ui.interaction.TvClickableSurface
import kotlinx.coroutines.launch

/**
 * A small "Paste" pill rendered above any text field that should accept a
 * one-tap clipboard injection. Solves the Android emulator's `adb shell input
 * text` char-drop bug, the keyboard-typing-on-TV pain, and the general "I just
 * copied a URL from my browser, why does this take 30 seconds to type" gripe.
 *
 * Reads the current clipboard via `LocalClipboard` and emits the text
 * verbatim through [onPaste]. Caller wires `onPaste = { fieldValue = it }`.
 *
 * If the clipboard is empty, the pill stays selectable but leaves the field
 * alone. Clearing is handled by [ClipboardClearButton].
 */
@Composable
fun ClipboardPasteButton(
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Paste from clipboard",
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    ClipboardActionPill(
        label = label,
        modifier = modifier,
        onClick = {
            scope.launch {
                val text = clipboard
                    .getClipEntry()
                    ?.clipData
                    ?.getItemAt(0)
                    ?.coerceToText(context)
                    ?.toString()
                    .orEmpty()
                if (text.isNotEmpty()) {
                    onPaste(text)
                }
            }
        }
    )
}

@Composable
fun ClipboardCopyButton(
    text: String,
    modifier: Modifier = Modifier,
    label: String = "Copy",
) {
    val clipboard = LocalClipboard.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    ClipboardActionPill(
        label = label,
        modifier = modifier,
        enabled = text.isNotEmpty(),
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))
            }
        }
    )
}

@Composable
fun ClipboardClearButton(
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Clear",
    enabled: Boolean = true,
) {
    ClipboardActionPill(
        label = label,
        modifier = modifier,
        enabled = enabled,
        onClick = onClear
    )
}

@Composable
private fun ClipboardActionPill(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AppColors.TiviAccentMuted,
            focusedContainerColor = AppColors.TiviAccent.copy(alpha = 0.34f),
            disabledContainerColor = AppColors.SurfaceAccent.copy(alpha = 0.32f),
            contentColor = AppColors.TiviAccentLight,
            focusedContentColor = AppColors.TextPrimary,
            disabledContentColor = AppColors.TextDisabled,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, AppColors.TiviAccent.copy(alpha = if (enabled) 0.36f else 0.12f)),
                shape = shape,
            ),
            focusedBorder = Border(
                border = BorderStroke(FocusSpec.BorderWidth, AppColors.Focus),
                shape = shape,
            ),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.padding(end = 2.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
