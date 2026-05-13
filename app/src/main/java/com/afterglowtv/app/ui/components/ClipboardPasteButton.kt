package com.afterglowtv.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors

/**
 * A small "Paste" pill rendered above any text field that should accept a
 * one-tap clipboard injection. Solves the Android emulator's `adb shell input
 * text` char-drop bug, the keyboard-typing-on-TV pain, and the general "I just
 * copied a URL from my browser, why does this take 30 seconds to type" gripe.
 *
 * Reads the current clipboard via `LocalClipboardManager` and emits the text
 * verbatim through [onPaste]. Caller wires `onPaste = { fieldValue = it }`.
 *
 * If the clipboard is empty, the pill is still clickable but [onPaste] is
 * called with an empty string — caller may want to ignore that. For
 * provider-setup URL fields we accept the empty string (it will clear the
 * field, which is occasionally what you want anyway).
 */
@Composable
fun ClipboardPasteButton(
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Paste from clipboard",
) {
    val clipboard = LocalClipboardManager.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AppColors.TiviAccentMuted)
            .clickable {
                val text = clipboard.getText()?.text.orEmpty()
                onPaste(text)
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            tint = AppColors.TiviAccentLight,
            modifier = Modifier.padding(end = 2.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.TiviAccentLight,
        )
    }
}
