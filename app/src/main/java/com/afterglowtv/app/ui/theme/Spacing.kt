package com.afterglowtv.app.ui.theme

import com.afterglowtv.app.ui.design.AppSpacing
import com.afterglowtv.app.ui.design.LocalAppSpacing

typealias Spacing = AppSpacing

val LocalSpacing = LocalAppSpacing

fun defaultSpacing(): Spacing = AppSpacing()
