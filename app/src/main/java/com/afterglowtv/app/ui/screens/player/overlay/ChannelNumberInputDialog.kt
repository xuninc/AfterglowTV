package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.GlowSpec
import com.afterglowtv.app.ui.design.afterglow

/**
 * On-screen numeric keypad for users without a numpad on their remote.
 *
 * D-pad navigable: focus moves between digit buttons (3×4 grid), the user
 * presses OK to enter a digit, and the dialog closes when they tap GO
 * (commit) or BACK (cancel). The dialog shows the current buffer + a
 * preview of which channel the input would jump to.
 *
 * The actual channel resolution happens in [com.afterglowtv.app.ui.screens.player.PlayerViewModel] —
 * this composable only manages keypad UI and delegates digit appends /
 * commit to the supplied callbacks. That keeps the same code path as
 * hardware numpad remotes (which already worked).
 *
 * @param currentBuffer The current numeric buffer being built (e.g. "20").
 * @param previewChannelName Name of the channel the buffer currently
 *     resolves to, or null if no match yet. Surfaces above the keypad so
 *     the user knows what they're about to jump to.
 * @param onDigitPressed Called with each digit (0-9) the user taps.
 *     Backed by [PlayerViewModel.appendNumericChannelDigit].
 * @param onClear Called when the user taps the Clear button. Should reset
 *     the buffer to empty.
 * @param onCommit Called when the user taps Go. Backed by
 *     [PlayerViewModel.commitNumericChannelInput].
 * @param onDismiss Called when the user backs out without committing.
 */
@Composable
fun ChannelNumberInputDialog(
    currentBuffer: String,
    previewChannelName: String?,
    onDigitPressed: (Int) -> Unit,
    onClear: () -> Unit,
    onCommit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(onClick = onDismiss),
            )

            // Dialog card
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.TiviSurfaceBase)
                    .border(
                        BorderStroke(1.dp, AppColors.TiviSurfaceAccent),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Go to channel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )

                // Big number display + preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.TiviSurfaceDeep)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = currentBuffer.ifEmpty { "—" },
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = TextUnit(40f, TextUnitType.Sp),
                        ),
                        color = AppColors.TiviAccent,
                        modifier = if (currentBuffer.isNotEmpty()) {
                            Modifier.afterglow(
                                specs = listOf(GlowSpec(AppColors.TiviAccent, 12.dp, 0.5f)),
                            )
                        } else Modifier,
                    )
                    Text(
                        text = previewChannelName?.takeIf { it.isNotBlank() } ?: "Enter a channel number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (previewChannelName != null) AppColors.TiviAccentLight else AppColors.TextTertiary,
                    )
                }

                // 3×4 keypad. We attach a FocusRequester to the "5" button so
                // the dialog opens with a button already focused — otherwise
                // the user has to D-pad once just to enter the focus tree, and
                // the entry point Compose picks is arbitrary.
                val initialFocusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    runCatching { initialFocusRequester.requestFocus() }
                }
                val rows = listOf(
                    listOf(KeypadKey.Digit(1), KeypadKey.Digit(2), KeypadKey.Digit(3)),
                    listOf(KeypadKey.Digit(4), KeypadKey.Digit(5), KeypadKey.Digit(6)),
                    listOf(KeypadKey.Digit(7), KeypadKey.Digit(8), KeypadKey.Digit(9)),
                    listOf(KeypadKey.Clear, KeypadKey.Digit(0), KeypadKey.Go),
                )
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { key ->
                            val attachInitialFocus = key is KeypadKey.Digit && key.value == 5
                            KeypadButton(
                                key = key,
                                enabled = key !is KeypadKey.Go || currentBuffer.isNotEmpty(),
                                focusRequester = if (attachInitialFocus) initialFocusRequester else null,
                                onPress = {
                                    when (key) {
                                        is KeypadKey.Digit -> onDigitPressed(key.value)
                                        KeypadKey.Clear -> onClear()
                                        KeypadKey.Go -> onCommit()
                                    }
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Press BACK to cancel",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextTertiary,
                )
            }
        }
    }
}

private sealed class KeypadKey {
    data class Digit(val value: Int) : KeypadKey()
    object Clear : KeypadKey()
    object Go : KeypadKey()
}

@Composable
private fun KeypadButton(
    key: KeypadKey,
    enabled: Boolean,
    focusRequester: FocusRequester?,
    onPress: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val (label, accent) = when (key) {
        is KeypadKey.Digit -> key.value.toString() to AppColors.TiviAccent
        KeypadKey.Clear -> "CLR" to AppColors.Warning
        KeypadKey.Go -> "GO" to AppColors.Live
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 64.dp)
            .let { base ->
                if (isFocused && enabled) {
                    base.afterglow(
                        specs = listOf(GlowSpec(accent, 16.dp, 0.65f)),
                        shape = shape,
                    )
                } else base
            }
            .clip(shape)
            .background(
                when {
                    !enabled -> AppColors.TiviSurfaceDeep
                    isFocused -> accent.copy(alpha = 0.25f)
                    else -> AppColors.TiviSurfaceCool
                }
            )
            .border(
                BorderStroke(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = when {
                        !enabled -> AppColors.TiviSurfaceAccent
                        isFocused -> accent
                        else -> AppColors.TiviSurfaceAccent
                    },
                ),
                shape,
            )
            .let { base -> if (focusRequester != null) base.focusRequester(focusRequester) else base }
            .focusable(enabled = enabled)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(enabled = enabled, onClick = onPress),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = TextUnit(22f, TextUnitType.Sp),
            ),
            color = when {
                !enabled -> AppColors.TextDisabled
                isFocused -> AppColors.TextPrimary
                else -> AppColors.TextSecondary
            },
        )
    }
}
