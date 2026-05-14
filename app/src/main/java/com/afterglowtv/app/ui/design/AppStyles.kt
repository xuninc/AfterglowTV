package com.afterglowtv.app.ui.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Reactive façade over the currently-active [AppShapeSet]. Mirrors how
 * [AppColors] works for the palette: read the current set via the
 * accessor `value`, swap an entire bundled set via [apply], or tweak
 * a single axis via the [setButton] / [setFocus] / [setProgress] /
 * etc. setters.
 *
 * Any composable that wants to switch its silhouette based on the user's
 * theme reads `AppStyles.value.button` (or any other component enum) and
 * renders the matching shape. The wrapper composables in
 * `app/ui/components/themed/` do this for you.
 */
object AppStyles {
    var value: AppShapeSet by mutableStateOf(AppShapeSet.Halo)
        private set

    fun apply(set: AppShapeSet) {
        value = set
    }

    fun setButton(style: AppShapeSet.ButtonStyle) { value = value.copy(button = style) }
    fun setEpgCell(style: AppShapeSet.EpgCellStyle) { value = value.copy(epgCell = style) }
    fun setEpgLiveCell(style: AppShapeSet.EpgLiveCellStyle) { value = value.copy(epgLiveCell = style) }
    fun setTextField(style: AppShapeSet.TextFieldStyle) { value = value.copy(textField = style) }
    fun setChannelRow(style: AppShapeSet.ChannelRowStyle) { value = value.copy(channelRow = style) }
    fun setPill(style: AppShapeSet.PillStyle) { value = value.copy(pill = style) }
    fun setFocus(style: AppShapeSet.FocusStyle) { value = value.copy(focus = style) }
    fun setProgress(style: AppShapeSet.ProgressStyle) { value = value.copy(progress = style) }
}
