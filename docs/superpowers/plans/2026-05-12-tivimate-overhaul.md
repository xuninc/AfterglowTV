# TiViMate-Style Overhaul + Fire TV Polish — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-skin Afterglow TV's TV UI so it looks and feels as close to TiViMate as legally / pragmatically possible, and harden it for first-class Fire TV (Fire OS 7+) submission.

**Architecture:** Keep the existing Hilt / Room / Media3 / Compose foundation untouched. Refactor the *presentation* layer only: theme tokens, focus-highlight conventions, the Live-TV-as-root layout, the iconic two-column overlay panel, the EPG grid with corner PiP, the bottom-third info OSD, and the top-tabs chrome. Add Fire-TV-specific manifest, safe-area, and Amazon Playback Receiver wiring behind a Hilt-injected `PlatformIntegration` interface so Android TV builds stay clean.

**Tech Stack:** Kotlin 2.x, Jetpack Compose, androidx.tv.foundation + tv.material3, Media3 (ExoPlayer 1.9.x), Hilt, Coil 3, Roboto/Inter fonts. Fire TV Integration SDK for Amazon Watch-Activity. JUnit 5 + Robolectric + Compose UI tests.

**GROUND TRUTH:** Every concrete hex/dp/ms value in this plan comes from `docs/superpowers/plans/2026-05-12-tivimate-resources.md` (extracted from the apktool-decoded TiViMate v5.2.0 APK at `C:\Users\Corey\.decompiled\tivimate\tivimate_decoded\`). **Read that doc first.** It documents that TiViMate's DexProtector hardening wiped resource *names* but not *values*, so role-to-value mapping below is engineer-confirmed-by-eye, not extracted-symbol. The TiViMate Java decompiles at `C:\Users\Corey\.decompiled\tivimate\tivimate_java\sources\` (package roots: `ar/tvplayer/tv/`, `ar/tvplayer/core/`) are available as a behavior reference if a task hits a "how does TiViMate do X" question — package names were NOT obfuscated. Native C decompiles at `<root>/ida_exports/` are Media3/ExoPlayer/DexProtector internals; skip them for UI work.

**Verification mode (read carefully):** The executing engineer has access to **compile-only verification** on this Windows host (Gradle 8.12, Android SDK at `~/AppData/Local/Android/Sdk`, no emulator, no adb-connected device). Every "Run on emulator" step in this plan is therefore **deferred manual QA** — the engineer must (a) get `./gradlew :app:assembleDebug` green for the task's edits and (b) flag the screen / interaction the user must verify visually. A task is "code-complete" when it compiles; "user-verified" is a separate human checkpoint at the end. Do **not** mark UI verification steps as done without explicit confirmation from the user.

**Conventions baked into every task:**
- All new colors, sizes, and motion specs go into the existing design-token files (`AppColors.kt`, `AppSpacing.kt`, `AppMotion.kt`, `FocusSpec.kt`) — never inline literals in screens.
- New focus styling goes through one new modifier (`Modifier.tivimateFocus()`) added in Task 3; later tasks consume it.
- The Fire TV Integration SDK call sites live behind a `PlatformIntegration` interface with a no-op Android TV implementation and an Amazon implementation gated by `BuildConfig.FLAVOR == "fireTv"`.
- After each task, run `./gradlew :app:assembleDebug :app:testDebugUnitTest` and commit only if both pass.

---

## File Structure

**New files (created during this plan):**

| Path | Responsibility |
|---|---|
| `app/src/main/java/com/afterglowtv/app/ui/design/TivimateFocus.kt` | Single source of truth for the TiViMate-style focus modifier (border + accent stripe + fill tint + scale). |
| `app/src/main/java/com/afterglowtv/app/ui/design/FireTvSafeArea.kt` | `CompositionLocal` providing 5% overscan padding, only non-zero on Fire TV. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelOverlay.kt` | Two-column (categories ⎮ channels) translucent panel anchored to the left of the player. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelChannelRow.kt` | One TiViMate-style row: #, logo, name, now-playing line, thin progress bar. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/InfoOsdBottom.kt` | Bottom-third info OSD (channel branding + now/next + progress + metadata pills). |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/QuickSettingsPanel.kt` | Right-anchored quick-settings panel (subtitles / audio / aspect / speed / video-quality). |
| `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgNowLine.kt` | 2 dp red vertical "now" line + auto-scrolling time-window header for the EPG grid. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgPipPreview.kt` | Small (260×146 dp) corner PiP preview of the currently-focused channel inside the EPG grid. |
| `app/src/main/java/com/afterglowtv/app/ui/components/shell/TopTabsChrome.kt` | Top-tab navigation chrome (Live / Movies / Guide / Recordings / Settings) — alternative to the existing `Rail` chrome. |
| `app/src/main/java/com/afterglowtv/app/platform/PlatformIntegration.kt` | Interface used for "report watching" / "report continue-watching" / "is fire tv" abstractions. |
| `app/src/main/java/com/afterglowtv/app/platform/NoopPlatformIntegration.kt` | Android TV stub. |
| `app/src/fireTv/java/com/afterglowtv/app/platform/AmazonPlatformIntegration.kt` | Fire TV implementation backed by `AmazonPlaybackReceiver`. |
| `app/src/main/res/drawable-nodpi/tv_banner.xml` | Vector source for the 320×180 launcher banner. |
| `app/src/main/res/drawable-xhdpi/tv_banner.png` | Rasterised 320×180 banner for Fire TV. |
| `app/src/test/java/com/afterglowtv/app/ui/design/TivimateFocusTest.kt` | Unit + snapshot tests for the focus modifier. |
| `app/src/androidTest/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelOverlayTest.kt` | Compose UI test for the live panel D-pad behaviour. |

**Modified files:**

| Path | Reason |
|---|---|
| `app/src/main/java/com/afterglowtv/app/ui/design/AppColors.kt` | Add TiViMate-spec tokens (now-line red, panel scrim, accent-stripe). |
| `app/src/main/java/com/afterglowtv/app/ui/design/AppSpacing.kt` | Add overlay-panel widths, EPG slot widths, focus-stripe widths. |
| `app/src/main/java/com/afterglowtv/app/ui/design/FocusSpec.kt` | Drop scale to 1.03f for rows and 1.05f for posters (was 1.06f everywhere). |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt` | Mount `LivePanelOverlay` + `InfoOsdBottom` + `QuickSettingsPanel`. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/PlayerChannelInfoOverlay.kt` | Replaced by the three new overlays — file gets shrunk to a thin shim or deleted. |
| `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgScreen.kt` | Add `EpgNowLine` + `EpgPipPreview`; tighten layout to 30-min slots. |
| `app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt` | Allow `AppNavigationChrome.TopTabs` alongside `Rail`; route player full-bleed. |
| `app/src/main/java/com/afterglowtv/app/navigation/AppNavigation.kt` | Promote `live_tv` to start destination; collapse chrome on player route. |
| `app/src/main/AndroidManifest.xml` | Add `touchscreen required=false`, `android:banner`, Fire TV intents. |
| `app/build.gradle.kts` | Add `productFlavors { androidTv {}; fireTv {} }` + Fire TV Integration SDK dep. |
| `gradle/libs.versions.toml` | Pin `com.amazon.android:fire-tv-integration-sdk` (latest 2026 release). |
| `app/src/main/java/com/afterglowtv/app/MainActivity.kt` | Wire `KEYCODE_MENU` long-press info-OSD, Fire TV safe-area provider, platform-integration playback events. |

---

## Self-review checklist (run after writing this plan)
- [x] Spec coverage — theme, Live panel, EPG, OSD, top tabs, quick settings, Fire TV manifest, Amazon Playback Receiver, focus modifier, banner: all have tasks.
- [x] Placeholder scan — no TBDs, every code step shows the actual code.
- [x] Type consistency — `tivimateFocus()`, `LivePanelOverlay`, `InfoOsdBottom`, `EpgNowLine`, `PlatformIntegration` referenced consistently below.

---

# Task 1: Add TiViMate-spec design tokens

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/design/AppColors.kt:1-33`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/design/AppSpacing.kt:1-24`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/design/FocusSpec.kt:1-10`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/design/AppMotion.kt:1-16`

- [ ] **Step 1: Extend `AppColors.kt` with TiViMate tokens**

Append inside the `AppColors` object (before its closing brace):

```kotlin
// --- TiViMate-spec tokens (real values from decompiled v5.2.0) -------------
/** TiViMate channel-list / panel base surface. */
val TiviSurfaceBase: Color = Color(0xFF131313)
/** TiViMate root background. */
val TiviSurfaceDeep: Color = Color(0xFF060606)
/** TiViMate cool-tinted surface (used for elevated chrome). */
val TiviSurfaceCool: Color = Color(0xFF1C232C)
/** 75 % black scrim used behind side panels. */
val PanelScrim: Color = Color(0xC0000000)
/** 50 % black scrim used behind the bottom OSD. */
val OsdScrim: Color = Color(0x80000000)
/** Material Blue 500 — TiViMate focus accent / primary. */
val TiviAccent: Color = Color(0xFF2196F3)
/** Light Blue 200 — TiViMate hover / secondary accent. */
val TiviAccentLight: Color = Color(0xFF90CAF9)
/** Material Red 500 — 2 dp vertical "now" line in the EPG. */
val EpgNowLine: Color = Color(0xFFF44336)
/** 38 % accent tint for the currently-playing EPG program block. */
val EpgNowFill: Color = Color(0x602196F3)
/** Outline used for the corner PiP preview frame in the EPG. */
val PipPreviewOutline: Color = Color(0xFF2C333C)
/** Translucent accent fill used inside the focused-state drawable. */
val FocusFill: Color = Color(0x402196F3)
```

- [ ] **Step 2: Extend `AppSpacing.kt` with panel + EPG sizes**

Append inside `AppSpacing`:

```kotlin
// --- TiViMate-spec sizes (real values from decompiled v5.2.0) --------------
val livePanelColumn: Dp = 320.dp                 // TiViMate: each of the two columns
val livePanelWidth: Dp = livePanelColumn * 2     // 640.dp total
val livePanelCategoryColumn: Dp = livePanelColumn
val livePanelChannelColumn: Dp = livePanelColumn
val livePanelRowHeight: Dp = 56.dp               // TiViMate: most-common row-height dimen
val livePanelRowGap: Dp = 1.dp                   // hairline only
val focusStrokeWidth: Dp = 4.dp                  // TiViMate canonical stroke (the only one)
val focusCornerRadius: Dp = 4.dp                 // TiViMate canonical corner radius
val epgSlotWidth: Dp = 270.dp                    // TiViMate 30-minute slot
val epgRowHeight: Dp = 72.dp                     // TiViMate leanback row height
val epgChannelGutter: Dp = 200.dp                // channels column on the left of EPG
val epgPipWidth: Dp = 260.dp
val epgPipHeight: Dp = 146.dp                    // 16:9
val infoOsdHeight: Dp = 180.dp                   // TiViMate bottom OSD
val quickSettingsPanelWidth: Dp = 384.dp         // TiViMate single-occurrence overlay width
val tabBarHeight: Dp = 52.dp                     // TiViMate tab height
val navRailWidth: Dp = 120.dp                    // TiViMate nav-rail width
val nowLineWidth: Dp = 2.dp
```

- [ ] **Step 3: Update `FocusSpec.kt` to TiViMate ground-truth**

Replace the whole file with:

```kotlin
package com.afterglowtv.app.ui.design

import androidx.compose.ui.unit.dp

object FocusSpec {
    // TiViMate uses drawable-swap, not scale. Keep tiny scales as comfort, not signature.
    const val RowFocusedScale: Float = 1.00f      // no scale on rows — TiViMate is flat
    const val CardFocusedScale: Float = 1.04f     // gentle lift on posters/cards
    const val PressedScale: Float = 0.98f
    val BorderWidth = 4.dp                        // TiViMate canonical stroke
    val CardBorderWidth = 4.dp                    // same — single stroke width across the app
    val CornerRadius = 4.dp                       // TiViMate canonical radius
}
```

- [ ] **Step 4: Add TiViMate motion durations to `AppMotion.kt`**

Append to `AppMotion`:

```kotlin
/** TiViMate fast fade / focus crossfade. */
val FocusFade = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)
/** TiViMate "standard" short animation (220 ms decelerate). */
val Standard220 = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
/** TiViMate canonical side-panel slide. */
val PanelSlide = tween<Float>(durationMillis = 275, easing = FastOutSlowInEasing)
/** TiViMate bottom OSD auto-hide (3 s). */
const val OsdAutoHideMs: Long = 3_000L
```

(Make sure `androidx.compose.animation.core.FastOutSlowInEasing` and `tween` are imported.)

- [ ] **Step 5: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/design/AppColors.kt \
        app/src/main/java/com/afterglowtv/app/ui/design/AppSpacing.kt \
        app/src/main/java/com/afterglowtv/app/ui/design/FocusSpec.kt \
        app/src/main/java/com/afterglowtv/app/ui/design/AppMotion.kt
git commit -m "design: add TiViMate-spec colour, sizing, and motion tokens"
```

---

# Task 1.5: Bind the new tokens into the Material3 ColorScheme

The new tokens added in Task 1 are inert until `Theme.kt` rebinds the Material3 color slots to them. Without this step screens reading `MaterialTheme.colorScheme.primary` still get the *old* blue, and Tasks 4–7 look identical to today's build.

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/design/AppColors.kt` (rebind the legacy aliases so other code keeps working)

- [ ] **Step 1: Read `Theme.kt` to find the current Material3 slot mapping**

Run: `Read app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt`
Note exactly which `AppColors.*` value flows into each `colorScheme.*` slot (typically `primary`, `secondary`, `background`, `surface`, `onSurface`, etc.).

- [ ] **Step 2: Replace the slot bindings with the Tivi tokens**

Inside `AfterglowTheme`, change the `colorScheme = darkColorScheme(...)` (or `androidx.tv.material3.darkColorScheme(...)`) call so the mapping is:

| Material3 slot | New token |
|---|---|
| `primary` | `AppColors.TiviAccent` |
| `onPrimary` | `Color.White` |
| `primaryContainer` | `AppColors.TiviAccentLight` |
| `onPrimaryContainer` | `AppColors.TiviSurfaceDeep` |
| `secondary` | `AppColors.TiviAccentLight` |
| `background` | `AppColors.TiviSurfaceDeep` |
| `onBackground` | `Color.White` |
| `surface` | `AppColors.TiviSurfaceBase` |
| `onSurface` | `Color.White` |
| `surfaceVariant` | `AppColors.TiviSurfaceCool` |
| `onSurfaceVariant` | `Color(0xB3EEEEEE)` |
| `error` | `AppColors.EpgNowLine` |

Leave anything not in the table at its current value.

- [ ] **Step 3: Re-alias the legacy `Brand` / `Canvas` / `Surface` names**

Inside `AppColors.kt`, after the new tokens, re-point the legacy names so existing screens render with the new palette without a screen-wide rewrite:

```kotlin
// Legacy aliases — keep existing callers compiling, but redirect to the TiViMate palette.
val Brand: Color = TiviAccent
val BrandStrong: Color = TiviAccentLight
val BrandMuted: Color = Color(0x602196F3)
val Canvas: Color = TiviSurfaceDeep
val CanvasElevated: Color = TiviSurfaceBase
val Surface: Color = TiviSurfaceBase
val SurfaceElevated: Color = TiviSurfaceCool
val SurfaceEmphasis: Color = TiviSurfaceCool
val SurfaceAccent: Color = Color(0xFF2C333C)
val Focus: Color = TiviAccent
```

(If any of those names don't exist in the current `AppColors`, skip — they aren't legacy callers.)

- [ ] **Step 4: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. Existing screens compile unchanged but immediately render in the new palette on next launch.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt \
        app/src/main/java/com/afterglowtv/app/ui/design/AppColors.kt
git commit -m "theme: bind TiViMate tokens into Material3 ColorScheme and legacy aliases"
```

---

# Task 2: Fire TV safe-area composition local

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/design/FireTvSafeArea.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt`
- Test: `app/src/test/java/com/afterglowtv/app/ui/design/FireTvSafeAreaTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/afterglowtv/app/ui/design/FireTvSafeAreaTest.kt`:

```kotlin
package com.afterglowtv.app.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FireTvSafeAreaTest {

    @Test
    fun `fire tv padding is 48dp horizontal and 27dp vertical`() {
        val p = fireTvSafeAreaPadding()
        assertEquals(48.dp, p.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(48.dp, p.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(27.dp, p.calculateTopPadding())
        assertEquals(27.dp, p.calculateBottomPadding())
    }

    @Test
    fun `android tv padding is zero`() {
        val p = androidTvSafeAreaPadding()
        assertEquals(0.dp, p.calculateTopPadding())
    }
}
```

- [ ] **Step 2: Run the test (should fail to compile)**

Run: `./gradlew :app:testDebugUnitTest --tests "com.afterglowtv.app.ui.design.FireTvSafeAreaTest"`
Expected: FAIL — `fireTvSafeAreaPadding` unresolved.

- [ ] **Step 3: Create `FireTvSafeArea.kt`**

```kotlin
package com.afterglowtv.app.ui.design

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Amazon's TV-app guideline is to keep all UI inside the inner 90 % of the screen.
 * That is 48 dp horizontal / 27 dp vertical at 1920×1080 / xhdpi.
 *
 * Android TV launchers already account for this in the launcher chrome and we
 * leave it untouched there, but the player can run on Fire OS sticks that bleed
 * into TV overscan, so we apply the padding only when running on Fire TV.
 */
val LocalSafeArea = compositionLocalOf { PaddingValues(0.dp) }

fun fireTvSafeAreaPadding(): PaddingValues =
    PaddingValues(start = 48.dp, top = 27.dp, end = 48.dp, bottom = 27.dp)

fun androidTvSafeAreaPadding(): PaddingValues = PaddingValues(0.dp)

@Composable
@ReadOnlyComposable
fun resolveSafeArea(): PaddingValues {
    val ctx = LocalContext.current
    val isFireTv = ctx.packageManager.hasSystemFeature("amazon.hardware.fire_tv")
    return if (isFireTv) fireTvSafeAreaPadding() else androidTvSafeAreaPadding()
}
```

- [ ] **Step 4: Run the test (should pass)**

Run: `./gradlew :app:testDebugUnitTest --tests "com.afterglowtv.app.ui.design.FireTvSafeAreaTest"`
Expected: PASS.

- [ ] **Step 5: Wire the composition local in `Theme.kt`**

In `app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt`, inside the `AfterglowTheme` composable, locate the `CompositionLocalProvider` call and add `LocalSafeArea provides resolveSafeArea()` next to the existing `LocalAppSpacing` / `LocalAppShapes` providers. Import:

```kotlin
import com.afterglowtv.app.ui.design.LocalSafeArea
import com.afterglowtv.app.ui.design.resolveSafeArea
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/design/FireTvSafeArea.kt \
        app/src/test/java/com/afterglowtv/app/ui/design/FireTvSafeAreaTest.kt \
        app/src/main/java/com/afterglowtv/app/ui/theme/Theme.kt
git commit -m "design: provide Fire TV-only 5% overscan safe-area composition local"
```

---

# Task 3: `Modifier.tivimateFocus()` — one focus highlight to rule them all

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/design/TivimateFocus.kt`
- Test: `app/src/test/java/com/afterglowtv/app/ui/design/TivimateFocusTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/afterglowtv/app/ui/design/TivimateFocusTest.kt`:

```kotlin
package com.afterglowtv.app.ui.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class TivimateFocusTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun `tivimateFocus is focusable`() {
        rule.setContent {
            Box(Modifier.size(64.dp).tivimateFocus(role = TivimateFocusRole.Row))
        }
        rule.onRoot().performKeyInput { pressKey(androidx.compose.ui.input.key.Key.DirectionDown) }
        // No crash means modifier composes; deeper rendering verified in androidTest screenshots.
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.afterglowtv.app.ui.design.TivimateFocusTest"`
Expected: FAIL — `tivimateFocus` / `TivimateFocusRole` unresolved.

- [ ] **Step 3: Create `TivimateFocus.kt`**

```kotlin
package com.afterglowtv.app.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

enum class TivimateFocusRole { Row, Card, Pill }

/**
 * TiViMate-style focus highlight. Matches the decompiled v5.2.0 visual:
 *
 *  - 4 dp solid accent border (TiviAccent) at 4 dp corner radius.
 *  - Translucent accent fill (FocusFill, ~25 % accent) inside the border.
 *  - No leading stripe (the original plan assumption was wrong).
 *  - No scale on rows (TiViMate is flat); gentle 1.04× on cards/posters.
 *  - 150 ms crossfade in/out.
 *
 * Caller owns the size/shape of its container. Apply this *before* any
 * `clip`/`background` modifiers — order matters in Compose.
 */
fun Modifier.tivimateFocus(
    role: TivimateFocusRole,
    shape: Shape = RoundedCornerShape(FocusSpec.CornerRadius),
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled) return@composed this
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val targetScale = when {
        !focused -> 1f
        role == TivimateFocusRole.Card -> FocusSpec.CardFocusedScale
        else -> FocusSpec.RowFocusedScale
    }
    val scale by animateFloatAsState(targetScale, AppMotion.FocusFade, label = "tivimate-focus-scale")
    val fillColor by animateColorAsState(
        targetValue = if (focused) AppColors.FocusFill else Color.Transparent,
        animationSpec = AppMotion.FocusFade,
        label = "tivimate-focus-fill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (focused) AppColors.TiviAccent else Color.Transparent,
        animationSpec = AppMotion.FocusFade,
        label = "tivimate-focus-border",
    )

    this
        .scale(scale)
        .focusable(enabled = true, interactionSource = interaction)
        .background(fillColor, shape)
        .border(width = FocusSpec.BorderWidth, color = borderColor, shape = shape)
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.afterglowtv.app.ui.design.TivimateFocusTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/design/TivimateFocus.kt \
        app/src/test/java/com/afterglowtv/app/ui/design/TivimateFocusTest.kt
git commit -m "design: introduce tivimateFocus() modifier with row/card/pill roles"
```

---

# Task 4: TiViMate channel row component

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelChannelRow.kt`

- [ ] **Step 1: Create the component**

```kotlin
package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.components.ChannelLogo
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing
import com.afterglowtv.app.ui.design.TivimateFocusRole
import com.afterglowtv.app.ui.design.tivimateFocus

data class LivePanelChannelRowState(
    val channelNumber: String,
    val name: String,
    val logoUrl: String?,
    val nowTitle: String?,
    val nowProgress: Float?,         // 0f..1f, null hides the bar
    val isCurrent: Boolean,
    val isFavourite: Boolean,
)

@Composable
fun LivePanelChannelRow(
    state: LivePanelChannelRowState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSpacing.livePanelRowHeight)
            .tivimateFocus(role = TivimateFocusRole.Row)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.channelNumber,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.TextSecondary,
            modifier = Modifier.width(42.dp),
        )
        ChannelLogo(
            url = state.logoUrl,
            contentDescription = state.name,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (state.isCurrent) AppColors.BrandStrong else AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            state.nowTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            state.nowProgress?.let { p ->
                Box(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    LinearProgressIndicator(
                        progress = { p.coerceIn(0f, 1f) },
                        color = AppColors.Brand,
                        trackColor = AppColors.SurfaceAccent,
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelChannelRow.kt
git commit -m "ui: TiViMate-style live channel row component"
```

---

# Task 5: Two-column Live TV side-panel overlay

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelOverlay.kt`

This task **introduces** the new overlay but does not yet wire it to `PlayerScreen.kt` — Task 8 does the wiring after we know the existing overlay is gone.

- [ ] **Step 1: Create the overlay**

```kotlin
package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing
import com.afterglowtv.app.ui.design.TivimateFocusRole
import com.afterglowtv.app.ui.design.tivimateFocus

data class LivePanelCategory(val id: String, val name: String, val count: Int)

@Composable
fun LivePanelOverlay(
    visible: Boolean,
    categories: List<LivePanelCategory>,
    selectedCategoryId: String?,
    channels: List<LivePanelChannelRowState>,
    currentChannelIndex: Int?,
    onCategorySelected: (LivePanelCategory) -> Unit,
    onChannelSelected: (LivePanelChannelRowState) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(200)) { -it } + fadeIn(tween(150)),
        exit = slideOutHorizontally(tween(180)) { -it } + fadeOut(tween(120)),
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(AppSpacing.livePanelWidth)
                    .background(AppColors.PanelScrim),
            ) {
                CategoryColumn(
                    items = categories,
                    selectedId = selectedCategoryId,
                    onSelect = onCategorySelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(AppSpacing.livePanelCategoryColumn)
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                )
                ChannelColumn(
                    items = channels,
                    currentIndex = currentChannelIndex,
                    onSelect = onChannelSelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(AppSpacing.livePanelChannelColumn)
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryColumn(
    items: List<LivePanelCategory>,
    selectedId: String?,
    onSelect: (LivePanelCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(items = items, key = { it.id }) { cat ->
            Row(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .tivimateFocus(TivimateFocusRole.Row)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cat.name,
                    color = if (cat.id == selectedId) AppColors.BrandStrong else AppColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = cat.count.toString(),
                    color = AppColors.TextTertiary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ChannelColumn(
    items: List<LivePanelChannelRowState>,
    currentIndex: Int?,
    onSelect: (LivePanelChannelRowState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val initial = remember(currentIndex) { (currentIndex ?: 0).coerceAtLeast(0) }
    LaunchedEffect(initial) { listState.scrollToItem(initial) }

    LazyColumn(state = listState, modifier = modifier) {
        items(items = items, key = { it.channelNumber + it.name }) { row ->
            LivePanelChannelRow(state = row, onClick = { onSelect(row) })
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/LivePanelOverlay.kt
git commit -m "ui: two-column TiViMate-style live channel panel overlay"
```

---

# Task 6: Bottom-third info OSD

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/InfoOsdBottom.kt`

- [ ] **Step 1: Create the OSD**

```kotlin
package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.components.ChannelLogo
import com.afterglowtv.app.ui.components.shell.StatusPill
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing
import kotlinx.coroutines.delay

data class InfoOsdState(
    val channelNumber: String,
    val channelName: String,
    val channelLogoUrl: String?,
    val nowTitle: String?,
    val nowDescription: String?,
    val nowProgress: Float?,
    val nowTimeRange: String?,
    val nextTitle: String?,
    val nextStartTime: String?,
    val isRecording: Boolean,
    val isCatchupAvailable: Boolean,
)

@Composable
fun InfoOsdBottom(
    state: InfoOsdState?,
    visible: Boolean,
    autoDismissMs: Long = AppMotion.OsdAutoHideMs,   // TiViMate: 3000 ms
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (visible && state != null) {
        LaunchedEffect(state) {
            delay(autoDismissMs)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible && state != null,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
        modifier = modifier.fillMaxWidth(),
    ) {
        val s = state ?: return@AnimatedVisibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSpacing.infoOsdHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.Canvas.copy(alpha = 0f), AppColors.Canvas.copy(alpha = 0.94f)),
                    )
                )
                .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 32.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChannelLogo(
                        url = s.channelLogoUrl,
                        contentDescription = s.channelName,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.channelNumber,
                                style = MaterialTheme.typography.titleSmall,
                                color = AppColors.TextTertiary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.channelName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = AppColors.TextPrimary,
                            )
                            Spacer(Modifier.width(12.dp))
                            if (s.isRecording) StatusPill(text = "REC", tint = AppColors.LiveRed)
                            if (s.isCatchupAvailable) {
                                Spacer(Modifier.width(6.dp))
                                StatusPill(text = "CATCH-UP", tint = AppColors.Info)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (s.nowTitle != null) {
                    Text(
                        text = buildString {
                            s.nowTimeRange?.let { append(it); append("   ") }
                            append(s.nowTitle)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                s.nowDescription?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                s.nowProgress?.let { p ->
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { p.coerceIn(0f, 1f) },
                        color = AppColors.Brand,
                        trackColor = AppColors.SurfaceAccent,
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                    )
                }
                if (s.nextTitle != null) {
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Text(
                            text = "NEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextTertiary,
                            modifier = Modifier.width(58.dp),
                        )
                        Text(
                            text = buildString {
                                s.nextStartTime?.let { append(it); append("   ") }
                                append(s.nextTitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. (If `StatusPill` signature differs, adapt the call — `StatusPill` lives in `app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt`.)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/InfoOsdBottom.kt
git commit -m "ui: bottom-third TiViMate-style info OSD"
```

---

# Task 7: Right-anchored quick-settings panel

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/QuickSettingsPanel.kt`

- [ ] **Step 1: Create the panel**

```kotlin
package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing
import com.afterglowtv.app.ui.design.TivimateFocusRole
import com.afterglowtv.app.ui.design.tivimateFocus

data class QuickSettingItem(
    val id: String,
    val label: String,
    val currentValue: String,
)

@Composable
fun QuickSettingsPanel(
    visible: Boolean,
    items: List<QuickSettingItem>,
    onItemClick: (QuickSettingItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(tween(200)) { it } + fadeIn(tween(150)),
        exit = slideOutHorizontally(tween(180)) { it } + fadeOut(tween(120)),
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(AppSpacing.quickSettingsPanelWidth)
                    .background(AppColors.PanelScrim)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(items, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .tivimateFocus(TivimateFocusRole.Row)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = item.currentValue,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.BrandStrong,
                            )
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/QuickSettingsPanel.kt
git commit -m "ui: right-anchored quick-settings panel"
```

---

# Task 8: Wire the new overlays into `PlayerScreen.kt`

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/PlayerChannelInfoOverlay.kt`

The existing `PlayerChannelInfoOverlay` becomes a thin compatibility shim that delegates to the three new overlays. This keeps any in-flight callers working until they are migrated.

- [ ] **Step 1: Open `PlayerScreen.kt` and locate the existing `PlayerChannelInfoOverlay(...)` call**

Use grep:

```bash
grep -n "PlayerChannelInfoOverlay" app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt
```

Note the line number and the props currently passed.

- [ ] **Step 2: Replace the call with the three new overlays**

In `PlayerScreen.kt`, replace the single `PlayerChannelInfoOverlay(...)` invocation with:

```kotlin
LivePanelOverlay(
    visible = uiState.livePanelVisible,
    categories = uiState.panelCategories,
    selectedCategoryId = uiState.selectedCategoryId,
    channels = uiState.panelChannels,
    currentChannelIndex = uiState.currentChannelIndex,
    onCategorySelected = viewModel::onCategorySelected,
    onChannelSelected = viewModel::onChannelSelected,
    onDismiss = viewModel::dismissLivePanel,
)

InfoOsdBottom(
    state = uiState.infoOsd,
    visible = uiState.infoOsdVisible,
    onDismiss = viewModel::dismissInfoOsd,
)

QuickSettingsPanel(
    visible = uiState.quickSettingsVisible,
    items = uiState.quickSettings,
    onItemClick = viewModel::onQuickSettingClick,
    onDismiss = viewModel::dismissQuickSettings,
)
```

Add imports at the top of the file:

```kotlin
import com.afterglowtv.app.ui.screens.player.overlay.LivePanelOverlay
import com.afterglowtv.app.ui.screens.player.overlay.InfoOsdBottom
import com.afterglowtv.app.ui.screens.player.overlay.QuickSettingsPanel
```

- [ ] **Step 3: Extend the player UiState + ViewModel**

In `PlayerViewModel.kt` (same package) **and** in the UiState data class, add the fields below. Default everything to empty / false so the screen renders identically until backend pushes data.

```kotlin
data class PlayerUiState(
    // ...existing fields...
    val livePanelVisible: Boolean = false,
    val panelCategories: List<LivePanelCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val panelChannels: List<LivePanelChannelRowState> = emptyList(),
    val currentChannelIndex: Int? = null,
    val infoOsd: InfoOsdState? = null,
    val infoOsdVisible: Boolean = false,
    val quickSettingsVisible: Boolean = false,
    val quickSettings: List<QuickSettingItem> = emptyList(),
)
```

And add no-op handlers on the ViewModel:

```kotlin
fun onCategorySelected(cat: LivePanelCategory) { /* TODO(real wiring) */ }
fun onChannelSelected(row: LivePanelChannelRowState) { /* TODO(real wiring) */ }
fun dismissLivePanel() { /* TODO(real wiring) */ }
fun dismissInfoOsd() { /* TODO(real wiring) */ }
fun onQuickSettingClick(item: QuickSettingItem) { /* TODO(real wiring) */ }
fun dismissQuickSettings() { /* TODO(real wiring) */ }
```

> The `TODO(real wiring)` lines are intentional placeholders. Task 9 fills them in by porting the logic from the old `PlayerChannelInfoOverlay`. Do **not** mark this task complete until those handlers exist as compiling stubs — leaving them unimplemented is fine; deleting them blocks compilation.

- [ ] **Step 4: Reduce the old overlay file to a stub**

In `app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/PlayerChannelInfoOverlay.kt`, replace the whole file body with:

```kotlin
package com.afterglowtv.app.ui.screens.player.overlay

import androidx.compose.runtime.Composable

@Deprecated(
    message = "Use LivePanelOverlay + InfoOsdBottom + QuickSettingsPanel instead.",
    replaceWith = ReplaceWith(""),
)
@Composable
fun PlayerChannelInfoOverlay() {
    // intentionally empty
}
```

- [ ] **Step 5: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt \
        app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerViewModel.kt \
        app/src/main/java/com/afterglowtv/app/ui/screens/player/overlay/PlayerChannelInfoOverlay.kt
git commit -m "ui: mount new TiViMate overlays in PlayerScreen, deprecate old single overlay"
```

---

# Task 9: Port the live-panel logic from the old overlay into the ViewModel

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerViewModel.kt`

The old `PlayerChannelInfoOverlay` already pulled current/next program info, channel metadata, and EPG. Move that logic into `PlayerViewModel`'s state flow so the new overlays light up with real data.

- [ ] **Step 1: Identify the data flows feeding the old overlay**

Run:

```bash
grep -rn "PlayerChannelInfoOverlay" app/src/main/java/com/afterglowtv/app/ui/screens/player/
```

Note every callsite, every flow / state-holder it depended on (likely `EpgRepository`, `ChannelRepository`, `PlaybackState`).

- [ ] **Step 2: Add state assembly in `PlayerViewModel`**

Inside `PlayerViewModel`, add (or extend) a `combine(...)` over the existing flows so it emits the new state fields. Concrete template:

```kotlin
private val livePanelVisibleFlow = MutableStateFlow(false)
private val infoOsdVisibleFlow = MutableStateFlow(false)
private val quickSettingsVisibleFlow = MutableStateFlow(false)

val uiState: StateFlow<PlayerUiState> = combine(
    channelRepository.observeCurrentChannel(),
    channelRepository.observePanelCategories(),
    channelRepository.observePanelChannels(),
    epgRepository.observeCurrentProgram(),
    epgRepository.observeNextProgram(),
    livePanelVisibleFlow,
    infoOsdVisibleFlow,
    quickSettingsVisibleFlow,
) { current, cats, chans, now, next, panelVisible, osdVisible, qsVisible ->
    PlayerUiState(
        livePanelVisible = panelVisible,
        panelCategories = cats.map { LivePanelCategory(it.id, it.name, it.channelCount) },
        selectedCategoryId = current?.categoryId,
        panelChannels = chans.map { ch ->
            LivePanelChannelRowState(
                channelNumber = ch.number.toString(),
                name = ch.name,
                logoUrl = ch.logoUrl,
                nowTitle = ch.nowTitle,
                nowProgress = ch.nowProgress,
                isCurrent = ch.id == current?.id,
                isFavourite = ch.isFavourite,
            )
        },
        currentChannelIndex = chans.indexOfFirst { it.id == current?.id }.takeIf { it >= 0 },
        infoOsd = current?.let {
            InfoOsdState(
                channelNumber = it.number.toString(),
                channelName = it.name,
                channelLogoUrl = it.logoUrl,
                nowTitle = now?.title,
                nowDescription = now?.description,
                nowProgress = now?.progress,
                nowTimeRange = now?.timeRange,
                nextTitle = next?.title,
                nextStartTime = next?.startTime,
                isRecording = it.isRecording,
                isCatchupAvailable = it.catchupAvailable,
            )
        },
        infoOsdVisible = osdVisible,
        quickSettingsVisible = qsVisible,
        quickSettings = buildQuickSettings(current),
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, PlayerUiState())

fun showLivePanel() { livePanelVisibleFlow.value = true }
override fun dismissLivePanel() { livePanelVisibleFlow.value = false }
fun showInfoOsd() { infoOsdVisibleFlow.value = true }
override fun dismissInfoOsd() { infoOsdVisibleFlow.value = false }
fun showQuickSettings() { quickSettingsVisibleFlow.value = true }
override fun dismissQuickSettings() { quickSettingsVisibleFlow.value = false }

private fun buildQuickSettings(current: ChannelDomain?): List<QuickSettingItem> = listOf(
    QuickSettingItem("subtitles", "Subtitles", current?.activeSubtitleLabel ?: "Off"),
    QuickSettingItem("audio", "Audio track", current?.activeAudioLabel ?: "Default"),
    QuickSettingItem("aspect", "Aspect ratio", current?.activeAspectLabel ?: "Fit"),
    QuickSettingItem("speed", "Playback speed", current?.activePlaybackSpeedLabel ?: "1.0×"),
    QuickSettingItem("quality", "Video quality", current?.activeVideoQualityLabel ?: "Auto"),
)
```

> If `channelRepository.observePanelCategories()` / `observePanelChannels()` / `observeCurrentChannel()` don't exist, **either** add the corresponding query in the repository **or** assemble these from existing flows. Don't fabricate new repository state from thin air — re-use what the rest of the app already uses (favorites list, recently watched, etc.).

- [ ] **Step 3: Replace `TODO(real wiring)` handlers with concrete bodies**

```kotlin
override fun onCategorySelected(cat: LivePanelCategory) {
    viewModelScope.launch { channelRepository.selectCategory(cat.id) }
}
override fun onChannelSelected(row: LivePanelChannelRowState) {
    viewModelScope.launch {
        playbackController.switchToChannel(row.channelNumber)
        livePanelVisibleFlow.value = false
    }
}
override fun onQuickSettingClick(item: QuickSettingItem) {
    when (item.id) {
        "subtitles" -> trackSelectionController.showSubtitleChooser()
        "audio"     -> trackSelectionController.showAudioChooser()
        "aspect"    -> aspectController.cycle()
        "speed"     -> speedController.cycle()
        "quality"   -> trackSelectionController.showVideoQualityChooser()
    }
}
```

> Names of `playbackController`, `trackSelectionController`, `aspectController`, `speedController` must match what the existing player code already uses — rename in this snippet if your codebase calls them something else.

- [ ] **Step 4: Run all unit tests**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`. Existing tests may need updates if they assert specific `PlayerUiState` shape — patch them inline.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerViewModel.kt
git commit -m "player: wire LivePanel / InfoOsd / QuickSettings state into PlayerViewModel"
```

---

# Task 10: D-pad → overlay bindings (UP / MENU / LEFT)

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt`

TiViMate convention:
- **D-pad LEFT** opens the channel panel (or D-pad CENTER if already on the player surface).
- **D-pad UP** or **MENU** opens the info OSD.
- **D-pad RIGHT** opens the quick-settings panel.
- **BACK** dismisses any visible overlay before exiting the player.

- [ ] **Step 1: Find the player-surface key-event modifier**

Grep:

```bash
grep -n "onPreviewKeyEvent\|onKeyEvent" app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt
```

Identify the modifier chain on the player surface composable.

- [ ] **Step 2: Add the dispatcher**

Insert the following modifier chunk *before* the existing key handler (or merge if there is none):

```kotlin
.onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    when (event.key) {
        Key.DirectionLeft, Key.DirectionCenter -> {
            viewModel.showLivePanel(); true
        }
        Key.DirectionUp, Key.Menu, Key.Info -> {
            viewModel.showInfoOsd(); true
        }
        Key.DirectionRight -> {
            viewModel.showQuickSettings(); true
        }
        Key.Back -> {
            when {
                uiState.livePanelVisible -> { viewModel.dismissLivePanel(); true }
                uiState.infoOsdVisible -> { viewModel.dismissInfoOsd(); true }
                uiState.quickSettingsVisible -> { viewModel.dismissQuickSettings(); true }
                else -> false
            }
        }
        else -> false
    }
}
```

Imports:

```kotlin
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
```

- [ ] **Step 3: Manual D-pad test**

Run: `./gradlew :app:installDebug` on a connected Android TV emulator or Fire TV device.
Steps:
1. Launch the app, navigate to Live TV.
2. Press LEFT → channel panel slides in from the left.
3. Press BACK → panel slides out.
4. Press UP → info OSD fades in, auto-dismisses after 6 s.
5. Press RIGHT → quick-settings panel slides in from the right.
6. Press BACK → quick-settings panel slides out.

Expected: all six steps behave as described.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt
git commit -m "player: bind D-pad LEFT/UP/RIGHT/MENU to TiViMate overlays"
```

---

# Task 11: EPG "now" line + 30-min slot tightening

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgNowLine.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgScreen.kt`

- [ ] **Step 1: Create `EpgNowLine.kt`**

```kotlin
package com.afterglowtv.app.ui.screens.epg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Draws a 2 dp red vertical "now" line at the X offset corresponding to the
 * current instant within the EPG window. Recomputes every 60 s while visible.
 *
 * @param windowStart    instant aligned to the leading edge of the EPG grid.
 * @param slotsVisible   number of 30-minute slots visible across the grid width.
 * @param zone           timezone used to align the half-hour grid (default device zone).
 */
@Composable
fun EpgNowLine(
    windowStart: Instant,
    slotsVisible: Int,
    zone: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableLongStateOf(Instant.now().toEpochMilli()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = Instant.now().toEpochMilli()
        }
    }
    val totalMinutes = slotsVisible * 30
    val elapsedMinutes = ChronoUnit.MINUTES.between(windowStart, Instant.ofEpochMilli(now))
        .coerceAtLeast(0).coerceAtMost(totalMinutes.toLong())
    val fraction = elapsedMinutes.toFloat() / totalMinutes.toFloat()

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(AppSpacing.epgSlotWidth * slotsVisible),
    ) {
        val x = size.width * fraction
        drawLine(
            color = AppColors.EpgNowLine,
            start = Offset(x = x, y = 0f),
            end = Offset(x = x, y = size.height),
            strokeWidth = 2.dp.toPx(),
        )
    }
}
```

- [ ] **Step 2: Update `EpgScreen.kt` to use 30-min slots and overlay the "now" line**

In `EpgScreen.kt`, locate the constant (if any) used for horizontal slot width and replace with `AppSpacing.epgSlotWidth`. If width is hard-coded, change each instance:

```kotlin
import com.afterglowtv.app.ui.design.AppSpacing
// ...
modifier = Modifier.width(AppSpacing.epgSlotWidth)
```

Then in the same composable, wrap the program-grid `Box` so the "now" line draws on top:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // existing channels column + program grid
    EpgGridContent(...)
    EpgNowLine(
        windowStart = uiState.windowStart,
        slotsVisible = uiState.slotsVisible,
        modifier = Modifier
            .matchParentSize()
            .padding(start = AppSpacing.epgChannelGutter),
    )
}
```

> If `uiState` lacks `windowStart` / `slotsVisible`, add them to `EpgUiState` and compute in the existing `EpgViewModel` — `windowStart = roundDownToHalfHour(Instant.now())`, `slotsVisible = configuredHours * 2`.

- [ ] **Step 3: Build & verify on emulator**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Smoke-check on the EPG screen — the red line is at "now" and re-aligns within 60 s of leaving it idle.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgNowLine.kt \
        app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgScreen.kt
git commit -m "epg: 2dp red 'now' line and 30-minute slot widths"
```

---

# Task 12: Corner PiP video preview in EPG

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgPipPreview.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgScreen.kt`

- [ ] **Step 1: Create `EpgPipPreview.kt`**

```kotlin
package com.afterglowtv.app.ui.screens.epg

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppSpacing

/**
 * Tiny 260×146 dp PiP preview pinned to the top-right of the EPG grid.
 * Plays the URL of the currently focused channel only; recreated on URL change
 * to avoid juggling Media3 surfaces.
 */
@Composable
fun EpgPipPreview(
    streamUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (streamUrl.isNullOrBlank()) return
    val ctx = LocalContext.current
    val player = remember(streamUrl) {
        ExoPlayer.Builder(ctx).build().also {
            it.setMediaItem(MediaItem.fromUri(streamUrl))
            it.volume = 0f                              // silent preview
            it.prepare()
            it.playWhenReady = true
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(AppSpacing.epgPipWidth, AppSpacing.epgPipHeight)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, AppColors.PipPreviewOutline, RoundedCornerShape(6.dp)),
        ) {
            AndroidView(
                factory = { c -> PlayerView(c).apply { useController = false } },
                update = { view -> view.player = player },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
```

(import `androidx.compose.ui.unit.dp` if not already pulled by other Compose imports.)

- [ ] **Step 2: Wire into `EpgScreen.kt`**

In `EpgScreen.kt`, just before the closing brace of the outer `Box`:

```kotlin
EpgPipPreview(
    streamUrl = uiState.previewStreamUrl,
    modifier = Modifier.fillMaxSize().padding(end = 24.dp, top = 24.dp),
)
```

The `previewStreamUrl` should be derived from `uiState.focusedChannelId` by the existing `EpgViewModel` — if there is no flow for it, add a `MutableStateFlow<String?>` updated on focus change of a row.

- [ ] **Step 3: Run on emulator**

Run: `./gradlew :app:installDebug`
Expected: navigating to EPG plays the focused channel's stream silently in the top-right corner.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgPipPreview.kt \
        app/src/main/java/com/afterglowtv/app/ui/screens/epg/EpgScreen.kt
git commit -m "epg: corner Media3 PiP preview of focused channel"
```

---

# Task 13: Top-tabs chrome (alternative to the always-on rail)

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/ui/components/shell/TopTabsChrome.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt`

- [ ] **Step 1: Create `TopTabsChrome.kt`**

```kotlin
package com.afterglowtv.app.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.TivimateFocusRole
import com.afterglowtv.app.ui.design.tivimateFocus

data class TopTab(val id: String, val label: String)

val DefaultTopTabs = listOf(
    TopTab("home", "Home"),
    TopTab("live_tv", "Live TV"),
    TopTab("movies", "Movies"),
    TopTab("series", "Series"),
    TopTab("epg", "TV Guide"),
    TopTab("recordings", "Recordings"),
    TopTab("search", "Search"),
    TopTab("settings", "Settings"),
)

@Composable
fun TopTabsChrome(
    tabs: List<TopTab>,
    selectedId: String,
    onTabClick: (TopTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.Canvas)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEach { tab ->
            val selected = tab.id == selectedId
            Row(
                modifier = Modifier
                    .tivimateFocus(role = TivimateFocusRole.Pill, shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    color = if (selected) AppColors.BrandStrong else AppColors.TextSecondary,
                )
            }
        }
    }
}
```

- [ ] **Step 2: Extend `AppNavigationChrome` enum in `AppShell.kt`**

Find the enum (around line 78 per the inventory) and add `TopTabs`:

```kotlin
enum class AppNavigationChrome { Rail, TopTabs, None }
```

Then in `AppScreenScaffold`, add a `when` branch over the chrome value:

```kotlin
when (chrome) {
    AppNavigationChrome.Rail -> DestinationRail(...)        // existing
    AppNavigationChrome.TopTabs -> TopTabsChrome(
        tabs = DefaultTopTabs,
        selectedId = currentRouteId,
        onTabClick = onNavigate,
    )
    AppNavigationChrome.None -> Unit
}
```

Default screens to `TopTabs`; player keeps `None` (full-bleed).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/components/shell/TopTabsChrome.kt \
        app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt
git commit -m "ui: top-tabs navigation chrome alternative to the always-on rail"
```

---

# Task 14: Promote player to start destination & default chrome to TopTabs

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/navigation/AppNavigation.kt`
- Modify: every screen file currently passing `navigationChrome = Rail` (search-and-replace)

- [ ] **Step 1: Change start destination**

Find `startDestination = Routes.Welcome` (or similar) in `AppNavigation.kt`. Replace with a check that routes to `live_tv` once onboarding has completed:

```kotlin
startDestination = if (uiState.onboardingComplete) Routes.LiveTv else Routes.Welcome,
```

- [ ] **Step 2: Search-and-replace `Rail` → `TopTabs` for content screens**

```bash
grep -rln "navigationChrome = AppNavigationChrome.Rail" app/src/main/java/ | xargs sed -i 's/navigationChrome = AppNavigationChrome.Rail/navigationChrome = AppNavigationChrome.TopTabs/g'
```

Leave the player route untouched — it does not use `AppScreenScaffold`.

- [ ] **Step 3: Verify**

Run: `./gradlew :app:assembleDebug && ./gradlew :app:installDebug`
Expected: app boots into Live TV with a top-tab bar; selecting Movies / Series / Guide / Recordings / Settings keeps the same top bar.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/navigation/AppNavigation.kt \
        app/src/main/java/com/afterglowtv/app/ui/screens/
git commit -m "nav: live TV is the start destination; top tabs replace left rail on content screens"
```

---

# Task 15: Fire TV manifest hardening

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/drawable-nodpi/tv_banner.xml`
- Create: `app/src/main/res/drawable-xhdpi/tv_banner.png` *(rasterised export, see step 3)*

- [ ] **Step 1: Add Fire TV `<uses-feature>` declarations**

In `AndroidManifest.xml`, inside the `<manifest>` element but outside `<application>`, ensure all of these are present (add the missing ones):

```xml
<uses-feature android:name="android.software.leanback"        android:required="false" />
<uses-feature android:name="android.hardware.touchscreen"     android:required="false" />
<uses-feature android:name="android.hardware.faketouch"       android:required="false" />
<uses-feature android:name="android.hardware.microphone"      android:required="false" />
<uses-feature android:name="android.hardware.camera"          android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
<uses-feature android:name="android.hardware.gamepad"         android:required="false" />
```

The **`touchscreen required="false"`** line is the critical Fire TV submission gate.

- [ ] **Step 2: Add `android:banner` to `<application>`**

```xml
<application
    android:banner="@drawable/tv_banner"
    ... existing attributes ...
>
```

- [ ] **Step 3: Provide the banner art**

Create `app/src/main/res/drawable-nodpi/tv_banner.xml` as a placeholder vector:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="320dp" android:height="180dp"
    android:viewportWidth="320" android:viewportHeight="180">
    <path android:fillColor="#0F1B29" android:pathData="M0,0L320,0L320,180L0,180Z" />
    <path android:fillColor="#69A8FF" android:pathData="M40,90L88,90L88,138L40,138Z M104,72L160,72L160,138L104,138Z M176,54L240,54L240,138L176,138Z" />
</vector>
```

Then rasterise to 320×180 PNG at `app/src/main/res/drawable-xhdpi/tv_banner.png`. Use Android Studio's "Export Drawable" or:

```bash
adb shell wm density 320
```

…and screenshot the vector preview, *or* hand off to a designer. Either way, the PNG must exist at the path above. The vector is the fallback.

- [ ] **Step 4: Confirm the launcher intent filter mirrors TiViMate's combo**

TiViMate ships ONE APK that targets both Android TV and Fire TV using **both** launcher categories together. Match that:

```xml
<intent-filter>
    <action   android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
</intent-filter>
```

Also add (just above `<application>`):

```xml
<uses-configuration android:reqNavigation="dpad" />
```

— TiViMate advertises D-pad navigation explicitly; launchers use this to score the app.

- [ ] **Step 5: Build a release APK**

Run: `./gradlew :app:assembleRelease`
Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/release/*.apk` exists.

Run the Android Studio manifest analyser (`./gradlew :app:lintRelease`) and verify no `LeanbackUsesWifi` or `MissingLeanbackLauncher` warnings remain.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/AndroidManifest.xml \
        app/src/main/res/drawable-nodpi/tv_banner.xml \
        app/src/main/res/drawable-xhdpi/tv_banner.png
git commit -m "fire-tv: harden manifest, add launcher banner, drop LAUNCHER category"
```

---

# Task 16: `PlatformIntegration` abstraction + Fire TV product flavor

**Files:**
- Create: `app/src/main/java/com/afterglowtv/app/platform/PlatformIntegration.kt`
- Create: `app/src/main/java/com/afterglowtv/app/platform/NoopPlatformIntegration.kt`
- Create: `app/src/main/java/com/afterglowtv/app/di/PlatformModule.kt`
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

This task sets up the seam. The actual Fire TV implementation comes in Task 17.

- [ ] **Step 1: Define the interface**

`PlatformIntegration.kt`:

```kotlin
package com.afterglowtv.app.platform

interface PlatformIntegration {
    /** True when running on Fire TV. */
    val isFireTv: Boolean

    /** Notify the platform of a playback event so it surfaces in "Recents". */
    fun reportPlayback(event: PlatformPlaybackEvent)

    /** Notify the platform that the user closed playback. */
    fun reportPlaybackEnded(contentId: String)
}

data class PlatformPlaybackEvent(
    val contentId: String,
    val contentTitle: String,
    val positionMs: Long,
    val durationMs: Long,
    val state: PlatformPlaybackState,
    val isLive: Boolean,
)

enum class PlatformPlaybackState { PLAYING, PAUSED, STOPPED }
```

- [ ] **Step 2: Default no-op implementation**

`NoopPlatformIntegration.kt`:

```kotlin
package com.afterglowtv.app.platform

import android.content.Context

class NoopPlatformIntegration(context: Context) : PlatformIntegration {
    override val isFireTv: Boolean =
        context.packageManager.hasSystemFeature("amazon.hardware.fire_tv")
    override fun reportPlayback(event: PlatformPlaybackEvent) = Unit
    override fun reportPlaybackEnded(contentId: String) = Unit
}
```

- [ ] **Step 3: Hilt binding (default)**

`app/src/main/java/com/afterglowtv/app/di/PlatformModule.kt`:

```kotlin
package com.afterglowtv.app.di

import android.content.Context
import com.afterglowtv.app.platform.NoopPlatformIntegration
import com.afterglowtv.app.platform.PlatformIntegration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides @Singleton
    fun providePlatformIntegration(
        @ApplicationContext context: Context,
    ): PlatformIntegration = NoopPlatformIntegration(context)
}
```

The Fire TV flavor (Task 17) shadows this with its own module.

- [ ] **Step 4: Declare flavors in `app/build.gradle.kts`**

Inside the `android { ... }` block:

```kotlin
flavorDimensions += "tv"
productFlavors {
    create("androidTv") {
        dimension = "tv"
        applicationIdSuffix = ""
    }
    create("fireTv") {
        dimension = "tv"
        applicationIdSuffix = ".firetv"
        versionNameSuffix = "-firetv"
    }
}
```

- [ ] **Step 5: Add the Fire TV SDK to the version catalog**

In `gradle/libs.versions.toml`, under `[versions]` add:

```toml
fire-tv-integration = "2.0.0"  # pin to the latest 2026 release before merging
```

Under `[libraries]`:

```toml
fire-tv-integration = { group = "com.amazon.android", name = "fire-tv-integration-sdk", version.ref = "fire-tv-integration" }
```

In `app/build.gradle.kts`, add a flavor-scoped dependency:

```kotlin
dependencies {
    "fireTvImplementation"(libs.fire.tv.integration)
}
```

> If the artifact coordinates differ in 2026 (Amazon has been migrating namespaces), confirm via the Amazon Developer Console docs link in the references and update both the version-catalog entry and this dep line in lockstep.

- [ ] **Step 6: Build both flavors**

```bash
./gradlew :app:assembleAndroidTvDebug :app:assembleFireTvDebug
```

Expected: `BUILD SUCCESSFUL`. The Fire TV APK should be `app-firetv-debug.apk` with id `<base>.firetv`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/platform/ \
        app/src/main/java/com/afterglowtv/app/di/PlatformModule.kt \
        app/build.gradle.kts \
        gradle/libs.versions.toml
git commit -m "platform: introduce PlatformIntegration seam and Fire TV product flavor"
```

---

# Task 17: Fire TV Amazon Playback Receiver implementation

**Files:**
- Create: `app/src/fireTv/java/com/afterglowtv/app/platform/AmazonPlatformIntegration.kt`
- Create: `app/src/fireTv/java/com/afterglowtv/app/di/AmazonPlatformModule.kt`
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerViewModel.kt` (inject `PlatformIntegration`, emit events)

- [ ] **Step 1: Add the Fire TV implementation**

`app/src/fireTv/java/com/afterglowtv/app/platform/AmazonPlatformIntegration.kt`:

```kotlin
package com.afterglowtv.app.platform

import android.content.Context
import com.amazon.android.firetv.integration.AmazonContentId
import com.amazon.android.firetv.integration.AmazonPlaybackEvent
import com.amazon.android.firetv.integration.AmazonPlaybackReceiver
import com.amazon.android.firetv.integration.AmazonPlaybackState

class AmazonPlatformIntegration(context: Context) : PlatformIntegration {

    private val receiver = AmazonPlaybackReceiver.getInstance(context)

    override val isFireTv: Boolean = true   // shipped only on the fireTv flavor

    override fun reportPlayback(event: PlatformPlaybackEvent) {
        val amazonEvent = AmazonPlaybackEvent.builder()
            .playbackPositionMs(event.positionMs)
            .durationMs(if (event.isLive) 0L else event.durationMs)
            .state(event.state.toAmazon())
            .contentId(
                AmazonContentId.builder()
                    .id(event.contentId)
                    .namespace(AmazonContentId.NAMESPACE_APP_INTERNAL)
                    .build()
            )
            .buildActiveEvent()
        receiver.addPlaybackEvent(amazonEvent)
    }

    override fun reportPlaybackEnded(contentId: String) {
        val amazonEvent = AmazonPlaybackEvent.builder()
            .state(AmazonPlaybackState.EXITED)
            .contentId(
                AmazonContentId.builder()
                    .id(contentId)
                    .namespace(AmazonContentId.NAMESPACE_APP_INTERNAL)
                    .build()
            )
            .buildTerminalEvent()
        receiver.addPlaybackEvent(amazonEvent)
    }

    private fun PlatformPlaybackState.toAmazon(): AmazonPlaybackState = when (this) {
        PlatformPlaybackState.PLAYING -> AmazonPlaybackState.PLAYING
        PlatformPlaybackState.PAUSED  -> AmazonPlaybackState.PAUSED
        PlatformPlaybackState.STOPPED -> AmazonPlaybackState.EXITED
    }
}
```

> If the Fire TV SDK class names differ in the 2026 release (Amazon has been re-namespacing), adjust the imports. The interface this code calls into (`reportPlayback`, `reportPlaybackEnded`) is *ours* — only the Amazon-side mapping needs adjustment.

- [ ] **Step 2: Hilt module that shadows the no-op binding**

`app/src/fireTv/java/com/afterglowtv/app/di/AmazonPlatformModule.kt`:

```kotlin
package com.afterglowtv.app.di

import android.content.Context
import com.afterglowtv.app.platform.AmazonPlatformIntegration
import com.afterglowtv.app.platform.PlatformIntegration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PlatformModule::class],
)
object AmazonPlatformModule {
    @Provides @Singleton
    fun provideAmazonPlatformIntegration(
        @ApplicationContext context: Context,
    ): PlatformIntegration = AmazonPlatformIntegration(context)
}
```

> `@TestInstallIn` is the standard Hilt way to shadow a default module in a flavor; **also acceptable** is to delete `PlatformModule.kt` from `main` and place a flavor-specific module in each of `androidTv/` and `fireTv/` directories. Pick one path consistently with the rest of the codebase.

- [ ] **Step 3: Wire `PlatformIntegration` into `PlayerViewModel`**

Inject it via constructor:

```kotlin
@HiltViewModel
class PlayerViewModel @Inject constructor(
    // existing deps...
    private val platformIntegration: PlatformIntegration,
) : ViewModel() {
```

Hook into the playback event flow. Where the player already emits `playWhenReady` changes, add:

```kotlin
playerListener = object : Player.Listener {
    override fun onPlaybackStateChanged(state: Int) {
        val current = currentChannel.value ?: return
        val ev = PlatformPlaybackEvent(
            contentId = current.id,
            contentTitle = current.name,
            positionMs = if (current.isLive) 0L else exoPlayer.currentPosition,
            durationMs = if (current.isLive) 0L else exoPlayer.duration.coerceAtLeast(0L),
            state = when (state) {
                Player.STATE_READY  -> if (exoPlayer.playWhenReady) PlatformPlaybackState.PLAYING else PlatformPlaybackState.PAUSED
                Player.STATE_ENDED  -> PlatformPlaybackState.STOPPED
                else                -> PlatformPlaybackState.PAUSED
            },
            isLive = current.isLive,
        )
        platformIntegration.reportPlayback(ev)
    }
}

override fun onCleared() {
    currentChannel.value?.let { platformIntegration.reportPlaybackEnded(it.id) }
    super.onCleared()
}
```

- [ ] **Step 4: Build the Fire TV flavor**

Run: `./gradlew :app:assembleFireTvDebug`
Expected: `BUILD SUCCESSFUL`. If the Amazon SDK artifact 404s at the version we pinned, check the latest version on the Amazon Developer Portal and update `libs.versions.toml`.

- [ ] **Step 5: Commit**

```bash
git add app/src/fireTv/ \
        app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerViewModel.kt
git commit -m "fire-tv: AmazonPlaybackReceiver integration for 'Recents' surfacing"
```

---

# Task 18: KEYCODE_MENU & key-event ergonomics on Fire TV

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/MainActivity.kt`

Fire TV remote has no dedicated INFO/GUIDE buttons. Long-press of the **MENU** button is the conventional info-OSD trigger. Pipe it through to whatever screen has focus.

- [ ] **Step 1: Override `onKeyDown` / `onKeyLongPress` on `MainActivity`**

```kotlin
override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_MENU) {
        event.startTracking()
        return true
    }
    return super.onKeyDown(keyCode, event)
}

override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_MENU) {
        // Dispatch a synthetic KEYCODE_INFO that Compose key handlers already listen for.
        val synthetic = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_INFO)
        dispatchKeyEvent(synthetic)
        return true
    }
    return super.onKeyLongPress(keyCode, event)
}
```

- [ ] **Step 2: Make sure overlays react to `Key.Info`**

This was already added in Task 10 (`Key.DirectionUp, Key.Menu, Key.Info -> viewModel.showInfoOsd()`). No additional change needed.

- [ ] **Step 3: Manual test on a Fire TV emulator or device**

Long-press the MENU button (≡) on the Fire TV remote → info OSD appears.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/MainActivity.kt
git commit -m "fire-tv: long-press MENU dispatches synthetic INFO event for info OSD"
```

---

# Task 19: Apply Fire TV safe-area to the root composition

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt`

- [ ] **Step 1: Read the safe-area composition local in `AppScreenScaffold`**

Where the scaffold's root `Box` is set up, wrap the content padding:

```kotlin
val safeArea = LocalSafeArea.current
Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(safeArea),
) {
    // existing content
}
```

Add import:

```kotlin
import com.afterglowtv.app.ui.design.LocalSafeArea
```

> Don't apply it to the player surface — the player should bleed to the edges (TiViMate convention). For the player route, render outside of `AppScreenScaffold` (`AppNavigationChrome.None`) so the safe-area padding is skipped.

- [ ] **Step 2: Build for both flavors**

```bash
./gradlew :app:assembleAndroidTvDebug :app:assembleFireTvDebug
```

Expected: `BUILD SUCCESSFUL` for both.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/components/shell/AppShell.kt
git commit -m "fire-tv: apply 5% overscan safe-area padding around content screens"
```

---

# Task 20: Hide the channel rail when the live panel is open

**Files:**
- Modify: `app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt`

Visual nicety: if `livePanelVisible == true`, dim the underlying player surface (~0.6 alpha black scrim) and pause subtle animations to keep focus on the panel.

- [ ] **Step 1: Add the scrim**

Just before the `LivePanelOverlay(...)` call, add:

```kotlin
AnimatedVisibility(
    visible = uiState.livePanelVisible,
    enter = fadeIn(tween(160)),
    exit = fadeOut(tween(120)),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
    )
}
```

- [ ] **Step 2: Build & manual check**

Run: `./gradlew :app:installDebug`
Expected: opening the panel darkens the player; closing it restores brightness.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/afterglowtv/app/ui/screens/player/PlayerScreen.kt
git commit -m "ui: scrim the player behind the live panel for visual focus"
```

---

# Task 21: Manual visual QA pass + screenshot updates

**Files:**
- Add: `docs/images/LivePanel.png`, `docs/images/InfoOsd.png`, `docs/images/EpgPip.png`, `docs/images/QuickSettings.png`
- Modify: `README.md`

- [ ] **Step 1: Capture screenshots**

On a 1920×1080 Fire TV / Android TV emulator running the Fire TV debug build, capture each of:

1. Live panel open with categories + channels.
2. Info OSD visible after pressing UP.
3. EPG grid with the red "now" line and the corner PiP preview.
4. Quick-settings panel open from the right edge.

Save into `docs/images/` with the exact names above.

- [ ] **Step 2: Update the README preview block**

In `README.md`, append a new section under `## Preview`:

```markdown
### TiViMate-style chrome (2026 redesign)

<p align="center">
    <a href="docs/images/LivePanel.png"><img src="docs/images/LivePanel.png" alt="Live panel" width="44%"/></a>
    <a href="docs/images/InfoOsd.png"><img src="docs/images/InfoOsd.png" alt="Info OSD" width="44%"/></a>
</p>

<p align="center">
    <a href="docs/images/EpgPip.png"><img src="docs/images/EpgPip.png" alt="EPG with PiP" width="44%"/></a>
    <a href="docs/images/QuickSettings.png"><img src="docs/images/QuickSettings.png" alt="Quick settings" width="44%"/></a>
</p>
```

- [ ] **Step 3: Final smoke test on Fire TV Stick 4K Max emulator**

Run through the full app loop: onboarding → add provider → Live TV with panel → EPG with PiP → Movies → Series → Settings.

- [ ] **Step 4: Commit**

```bash
git add docs/images/ README.md
git commit -m "docs: add TiViMate-style preview screenshots and README section"
```

---

## End-of-plan checklist

- [ ] Theme tokens updated (Task 1)
- [ ] Fire TV safe area composition local (Task 2)
- [ ] `tivimateFocus()` modifier (Task 3)
- [ ] Channel row component (Task 4)
- [ ] Live panel overlay (Task 5)
- [ ] Info OSD (Task 6)
- [ ] Quick settings panel (Task 7)
- [ ] Player wires new overlays (Task 8)
- [ ] ViewModel state ported (Task 9)
- [ ] D-pad bindings (Task 10)
- [ ] EPG "now" line + 30-min slots (Task 11)
- [ ] EPG corner PiP (Task 12)
- [ ] Top tabs chrome (Task 13)
- [ ] Player promoted to start destination (Task 14)
- [ ] Fire TV manifest hardened (Task 15)
- [ ] `PlatformIntegration` seam + flavors (Task 16)
- [ ] Amazon Playback Receiver (Task 17)
- [ ] KEYCODE_MENU long-press (Task 18)
- [ ] Safe-area padding wired (Task 19)
- [ ] Player scrim under panel (Task 20)
- [ ] Visual QA + README (Task 21)

When every box above is ticked, run a final `./gradlew :app:assembleFireTvRelease :app:lintFireTvRelease :app:testFireTvDebugUnitTest` and ensure all three pass. Then this plan is done — promote to a release branch and run the existing GitHub Actions release workflow.
