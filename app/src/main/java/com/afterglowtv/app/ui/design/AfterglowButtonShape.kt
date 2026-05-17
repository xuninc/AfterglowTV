package com.afterglowtv.app.ui.design

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun afterglowButtonShape(style: AppShapeSet.ButtonStyle): Shape =
    when (style) {
        AppShapeSet.ButtonStyle.PILL -> RoundedCornerShape(999.dp)
        AppShapeSet.ButtonStyle.SHARP -> RoundedCornerShape(0.dp)
        AppShapeSet.ButtonStyle.SOFT -> RoundedCornerShape(8.dp)
        AppShapeSet.ButtonStyle.CUT_CORNER -> GenericShape { size, _ ->
            val cut = 12f.coerceAtMost(size.minDimension / 3f)
            moveTo(cut, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - cut, size.height)
            lineTo(0f, size.height)
            lineTo(0f, cut)
            close()
        }
        AppShapeSet.ButtonStyle.STRIPE -> RoundedCornerShape(3.dp)
        AppShapeSet.ButtonStyle.GHOST -> RoundedCornerShape(4.dp)
        AppShapeSet.ButtonStyle.GLASS -> RoundedCornerShape(6.dp)
        AppShapeSet.ButtonStyle.DOUBLE_SHADOW -> RoundedCornerShape(2.dp)
    }
