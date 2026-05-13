package com.afterglowtv.app.ui.design

import androidx.compose.ui.unit.dp

object FocusSpec {
    // Existing constants kept compiling — value tuned closer to TiViMate's flatter look.
    const val FocusedScale = 1.04f
    const val PressedScale = 0.98f
    val BorderWidth = 4.dp                  // TiViMate canonical stroke (was 3.dp)
    val CardBorderWidth = 4.dp              // matched to TiViMate single stroke

    // TiViMate-specific roles for the new tivimateFocus() modifier.
    const val RowFocusedScale = 1.00f       // TiViMate rows are flat — drawable swap only.
    const val CardFocusedScale = 1.04f      // gentle lift on posters/cards.
    val CornerRadius = 4.dp                 // TiViMate canonical corner radius.
}
