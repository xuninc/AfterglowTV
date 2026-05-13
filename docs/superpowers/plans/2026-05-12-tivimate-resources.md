# TiViMate Resource Cheat Sheet — Ground Truth Extract

> Reference for the Afterglow TV re-skin. Specifications only — no copyrighted assets are reproduced.
> Source APK: `tivimate.apk` v5.2.0 (5208), package `ar.tvplayer.tv`, compileSdk 36 (Android 16).

## CRITICAL CAVEAT — Read First

The TiViMate APK is hardened with **DexProtector**. The string-pool for resources has been wiped: every resource name in `res/values/*.xml` and every drawable / layout / anim filename in `res/` has been replaced with `APKTOOL_RENAMED_0x7f0XXXXX` or `APKTOOL_DUMMY_0x7f0XXXXX`. The hex *values*, dp *numbers*, layout *structure*, and animation *durations* are real and trustworthy. The semantic *names* (e.g. "panel_width", "epg_now_line") **do not exist in the artifact** — they were deleted from the ARSC string pool. The pre-digested `branding.txt` / `useful_features.txt` files in the parent dir are raw multi-tool `grep` concordances of every source file, not curated indexes; they cannot tell us which `0x7f060XXX` is which color.

What this means for implementation: treat this document as a **palette and metric quarry**, not a name-to-role map. The visual roles (panel scrim, focus accent, EPG now-line red) must be matched by looking at the running app (or screenshots) and picking the closest hex from the palettes below. Anything tagged "(role inferred)" is the engineer's best guess, not extracted symbol meaning.

Source root: `C:\Users\Corey\.decompiled\tivimate\tivimate_decoded\` (referred to below as `<root>/`).

---

## 1. Colors — Hex Palette

`<root>/res/values/colors.xml` contains 1065 lines / 208 unique hex literals. The semantic names are stripped. Below: the values most likely to drive the TiViMate look, grouped by family.

### 1a. Background / surface (very dark — these are the chrome of TiViMate)

| Hex | Notes (role inferred) |
|---|---|
| `#000000` | Pure black — video-letterbox fallback (10 occurrences, most-used color). |
| `#060606` | Near-black — likely root app background. |
| `#121212` | Material "dark surface" — likely default surface. |
| `#131313` | Channel-list row background / overlay base. |
| `#131619` | Cool near-black surface variant. |
| `#161616` / `#1b1b1b` / `#202020` / `#262626` / `#323232` | Stepped neutral elevations (surface → surface-3). |
| `#1c232c` / `#2c333c` / `#3c434c` | Cool blue-grey surface family (matches TiViMate's blue-tinted dark theme). |
| `#0e2d2a` / `#0f2c2f` / `#164440` / `#174247` | Deep teal — likely "premium" / paid panel accents. |
| `#142b3d` / `#1f415c` / `#29577b` | Deep blue — likely EPG panel / live overlay scrim. |
| `#151b3f` / `#1f295f` / `#2a377f` / `#2e4677` / `#304f8b` | Indigo family (poster / VOD chrome, speculative). |

### 1b. Accent / focus / brand

| Hex | Notes (role inferred) |
|---|---|
| `#2196f3` | Material Blue 500 — confirmed in palette twice. Likely **focus accent / primary**. |
| `#2daae2` / `#2788be` / `#2588b5` | Lighter cyan-blue — focus glow / progress bar gradient family. |
| `#90caf9` | Light blue 200 — secondary accent / pressed state. |
| `#03dac6` / `#00bcd4` / `#009688` | Teal family — Material 3 secondary defaults (probably unused in production UI). |
| `#ffc107` / `#ffe082` / `#fff59d` | Amber family — "premium" badge / star ratings (speculative). |

### 1c. EPG / now-line / live indicator (speculative — pick from these)

TiViMate uses a thin red "now" line in the EPG. The reds in the palette:

| Hex | Notes |
|---|---|
| `#f44336` (and `#60f44336` 38 % alpha) | Material Red 500 — most likely the now-line color. |
| `#e91e63` / `#60e91e63` | Pink 500 — alternative live badge. |
| `#ffeb3b` / `#60ffeb3b` | Yellow accent (not red — used for warning/recording). |

### 1d. Translucent overlays (panels & scrims)

| Hex | Notes |
|---|---|
| `#80000000` (50 %) / `#90000000` (56 %) / `#c0000000` (75 %) | Bottom-OSD scrim candidates. |
| `#60000000` (38 %) / `#39000000` (22 %) / `#1f000000` (12 %) / `#14000000` (8 %) | Hover / panel-base scrims. |
| `#80ffffff` / `#b3ffffff` / `#1affffff` / `#33ffffff` | White-on-dark text overlay tints. |
| `#8a000000` (54 %) / `#de000000` (87 %) | Material primary/secondary text-on-light alphas. |

### 1e. Text

| Hex | Notes |
|---|---|
| `#ffffff` | Primary text on dark. |
| `#eeeeee` / `#ffeeeeee` / `#b3eeeeee` (70 %) / `#80eeeeee` (50 %) / `#4deeeeee` (30 %) | Stepped white tints — primary / secondary / disabled / hint. |
| `#bdbdbd` / `#cccccc` / `#d6d7d7` | Mid grey labels. |

### 1f. Night-mode overrides
`<root>/res/values-night/` contains **only** `styles.xml` (23 style-rebind shims, no `colors.xml`). TiViMate ships its dark theme via the *default* palette — there is no light variant in production.

**File refs:** `<root>/res/values/colors.xml`, `<root>/res/values-night/styles.xml`.

---

## 2. Dimensions — dp/sp Cheat Sheet

`<root>/res/values/dimens.xml` — 1392 entries, names obfuscated. Below: the histogram (what dp values appear how often), plus the **screen-region-class** dimensions.

### 2a. Spacing histogram (counts of distinct entries at each value)

| Value | Count | Likely role |
|---|---|---|
| `8.0dp` | 53 | default gap / row padding |
| `4.0dp` | 45 | corner radius / hairline gap |
| `24.0dp` | 39 | section padding |
| `16.0dp` | 37 | content padding (matches `<dimen name="u">` = grid unit) |
| `2.0dp` | 34 | focus stroke / hairline divider |
| `12.0dp` | 26 | row inset |
| `32.0dp` | 23 | header padding |
| `56.0dp` | 20 | row height (Material list-row) |
| `48.0dp` | 17 | focusable min height |
| `6.0dp` | 16 | small inset |
| `1.0dp` | 16 | divider |
| `64.0dp` | 12 | logo cell |
| `52.0dp` | 10 | tab height |
| `40.0dp` / `36.0dp` | 9 | tab/pill height |
| `72.0dp` | 6 | leanback row height |
| `132.0dp` | 5 | channel-logo width (sw600 land) |
| `270.0dp` | 5 | poster/EPG slot width |
| `96.0dp` | 5 | thumbnail |
| `120.0dp` | 4 | nav-rail width |
| `320.0dp` | 4 | overlay panel width |

### 2b. Text size (sp) histogram

| sp | Count | Likely role |
|---|---|---|
| `14sp` | 15 | body / row title |
| `12sp` | 9 | caption / metadata |
| `16sp` | 5 | section header |
| `18sp` | 3 | dialog title |
| `20sp` | 2 | OSD channel name |
| `22sp` | 1 | EPG program title |
| `28sp` | 1 | dialog headline |
| `24sp` | 1 | onboarding heading |
| `34sp` | 1 | hero text |
| `44sp` | 1 | display |
| `10sp` / `15sp` | 1 each | misc |

### 2c. Region-class dimensions (every dp ≥ 100, single occurrences are the load-bearing layout numbers)

| Value | Count of distinct entries | Likely role |
|---|---|---|
| `720.0dp` | 1 | sw720 breakpoint / dialog max width |
| `668.0dp` / `640.0dp` / `584.0dp` / `540.0dp` / `536.0dp` / `488.0dp` | 1 each | dialog / settings panel widths |
| `408.0dp` / `400.0dp` / `384.0dp` / `380.0dp` / `360.0dp` | 1 each | overlay panel widths |
| `320.0dp` | 4 | **two-column overlay: each column ≈ 320 dp wide → 640 dp total** |
| `296.0dp` / `294.0dp` / `284.0dp` / `280.0dp` / `274.0dp` / `264.0dp` / `256.0dp` / `252.0dp` / `248.0dp` / `238.0dp` | 1 each | poster / card / EPG slot widths |
| `224.0dp` / `220.0dp` / `210.0dp` / `200.0dp` / `190.0dp` / `188.0dp` / `180.0dp` | 1 each | thumb / panel sub-region widths |
| `168.0dp` / `164.0dp` / `159.0dp` / `147.0dp` / `140.0dp` / `132.0dp` (×4) / `128.0dp` / `124.0dp` / `120.0dp` (×4) / `110.0dp` / `104.0dp` / `100.0dp` | varies | row / logo / chip widths |

### 2d. Percentage dimensions

| Value | Likely role |
|---|---|
| `79.99999%` / `100.0%` | Dialog width fractions |
| `65.0%` / `95.00001%` | Sw600 dialog fractions |

### 2e. Negative dimensions (used as inset / overlap)
`-1.0dp`, `-3.0dp`, `-4.0dp`, `-12.0dp`, `-100.0dp` — typically scroll-edge overlap or focus-stripe overhang. The `-100.0dp` is striking — likely an off-screen slide-in initial position.

**File refs:** `<root>/res/values/dimens.xml`.

---

## 3. Text Styles

Style names are obfuscated. `<root>/res/values/styles.xml` is 5742 lines; parent-style frequency analysis:

| Frequency | Parent style | What it means |
|---|---|---|
| 50 | `@style/<obfuscated 0x7f1404bf>` | The app's most-used custom parent (likely a base `Widget.TiviMate.*`). |
| 39 | `@android:style/Widget` | Direct framework widget base. |
| 17 | `<obfuscated 0x7f140293>` | A custom sub-parent (likely `TextAppearance.TiviMate.Body`). |
| 12 | `<obfuscated 0x7f140252>` | Another sub-parent (Title?). |
| 11 | `<obfuscated 0x7f140151>` | Another sub-parent. |
| 10 | `<obfuscated 0x7f140191>` | Another sub-parent. |
| 8 | `@android:style/TextAppearance` | Direct framework text base. |
| 5 | `@android:style/Animation` | Window-animation styles (see §8). |

Recognisable framework parents that **were** kept:
| Style | Parent |
|---|---|
| `0x7f140090` | `@android:style/Theme.Material.Dialog.Alert` |
| `0x7f140091` | `@android:style/Theme.Material.Light.Dialog.Alert` |
| `0x7f140182` | `@android:style/Theme.NoTitleBar` |
| `0x7f14018c` | `@android:style/Theme.Material.NoActionBar` |
| `0x7f14018d` | `@android:style/Theme.Material.Light.NoActionBar` |

→ Production app theme is **Theme.Material.NoActionBar** flavour, not MaterialComponents/Material3. Text-appearance tokens we can extract directly from `dimens.xml` text-size mapping (§2b): body 14 sp, caption 12 sp, header 16 sp, hero 20–22 sp. Roboto Medium (see §5) is the loaded weight.

**File refs:** `<root>/res/values/styles.xml`, `<root>/res/values-night/styles.xml`.

---

## 4. Shapes / Corner Radii / Strokes

Aggregated across all `<root>/res/drawable/*.xml` shape XMLs:

| Specification | Value(s) found | Notes |
|---|---|---|
| Corner radius (literal in XML) | `2.0dp`, `3.0dp`, `4.0dp`, `7.0dp`, `10.0dp`, `16.0dp` | `4.0dp` is canonical (matches most-used corner-radius dimen, count 45 in §2a). |
| Stroke width | `4.0dp` (4 occurrences — only literal stroke width) | TiViMate's signature thick focus border. |
| Inset wrappers (`<inset>`) | `insetLeft/Right` = `0x7f070012` (4 dp), `insetTop/Bottom` = `0x7f070013` (6 dp) | Button hit-area insets. |
| Common solid fills | `@android:color/white`, `@android:color/transparent`, `#1f000000`, `#ebebeb`, `#000000` | Button surfaces & overlay scrims. |

Shapes that reference dimen tokens (resolve via §2):
- `<corners radius="@dimen/0x7f070018">` → `2.0dp`
- `<corners radius="@dimen/0x7f070065">` → ~`4.0dp` (common-radius family)
- `<corners radius="@dimen/0x7f0700d3">` → larger pill (likely 12+ dp)
- `<corners radius="@dimen/0x7f07050c">` → very large (chip)

**Implementation hint:** card/button radius = **4 dp**, focus border stroke = **4 dp**, pill chip radius = **12 dp**. These are the only thicknesses meaningfully present.

**File refs:** `<root>/res/drawable/` (166 xml files).

---

## 5. Fonts

| File | Detected |
|---|---|
| `<root>/res/font/u.ttf` | TrueType, 14 tables. Name table reports: `"Copyright 2011 Google Inc. All Rights Reserved. Roboto Medium / Regular / Version 2.137; 2017 / Roboto-Med..."` |

→ The single shipped font is **Roboto Medium (weight 500)**. No Inter, no custom. Other weights are pulled from the system. Afterglow TV can use Google-Fonts Roboto Medium freely (Apache 2.0, no trademark issue).

**File refs:** `<root>/res/font/u.ttf` (3316 bytes).

---

## 6. Layouts — Structure Hints

`<root>/res/layout/` has 289 files; `<root>/res/layout-land/`, `layout-sw600dp/`, `layout-v26/`, `layout-watch/` also present. Names are obfuscated, so individual screens can't be located by name from the artifact alone. What we **can** say from the dimens.xml + selector evidence:

| Region | Spec (inferred) |
|---|---|
| Two-column overlay (categories \| channels) | Each column **320 dp** wide → 640 dp total. Translucent dark scrim over video. (Backed by §2c — `320.0dp` appears 4 times, no other 320-vicinity width is repeated.) |
| Channel-list row height | **52–56 dp** (most common row-height dimens) with **8 dp** horizontal padding. |
| Channel-row logo cell | **64 dp** wide (12 occurrences in dimens). |
| Channel number column | ~**40 dp** wide (1 dimen entry). |
| EPG slot/poster card width | **270 dp** or **264–296 dp** — pick **270 dp** as the canonical slot. (One 30-min slot.) |
| EPG row / channel-strip height | ~**72 dp** (leanback default; 6 entries). |
| Bottom info OSD height | ~**168–190 dp** (single-occurrence dimens near `1/3` of 720 dp). |
| PiP preview (corner) | ~**260 × 146 dp** (one 256-dp width + 16:9 ratio). Speculative — there is no symbol confirming. |
| Tabs / top chrome height | **52 dp** (most common tab-height dimen). |
| Nav rail width | **120 dp** (4 entries). |
| Dialog widths | Phone: `79.999%` width fraction. Sw600: `65.0%`. Hard cap **720 dp**. |
| Sw720 land breakpoint | `720.0dp` dimen — TiViMate explicitly supports sw720. |

> Speculation flag: every "role inferred" mapping in this table must be confirmed against the running app before being baked into Afterglow TV tokens — the symbol pool that would have authoritatively labelled these is gone.

**File refs:** `<root>/res/layout/`, `<root>/res/layout-land/`, `<root>/res/layout-sw600dp/`.

---

## 7. Drawable Selectors — Focus Convention

24 drawables in `<root>/res/drawable/` and `<root>/res/drawable-anydpi/` contain `<item android:state_focused="true" ...>` selectors. The pattern is consistent:

```xml
<selector>
    <item state_focused="true"  android:drawable="@drawable/<focus-fill>" />
    <item state_pressed="true"  android:drawable="@drawable/<pressed-fill>" />
    <item                       android:drawable="@android:color/transparent" />
</selector>
```

Multi-state variant (e.g. `<root>/res/drawable/APKTOOL_RENAMED_0x7f080053.xml`):

```xml
<item state_focused="true" state_enabled="false" state_pressed="true"   drawable=<disabled-focus-pressed> />
<item state_focused="true" state_enabled="false"                         drawable=<disabled-focus> />
<item state_focused="true" state_pressed="true"                          drawable=<focus-pressed> />
<item state_focused="false" state_pressed="true"                         drawable=<pressed> />
<item state_focused="true"                                               drawable=<focused> />
<item                                                                    drawable=transparent />
```

Another variant uses **color references** rather than drawables (e.g. `0x7f080098`):
```xml
<item state_pressed="true"  drawable="@color/<pressed-tint>" />
<item state_focused="true"  drawable="@color/<focus-tint>" />
<item                       drawable="@color/<rest-tint>" />
```

**Conclusion for Afterglow TV:** TiViMate focus is delivered as **a swap-the-background-drawable selector**, not a runtime tint. The focused drawable is a rectangle with **4 dp corner radius** and **4 dp stroke** (the only stroke width found, §4), filled with a translucent accent over the dark surface. Replicate by giving every focusable row/card a `Modifier.tivimateFocus()` that draws a 4 dp inset border + 4 dp radius + accent fill ramp.

**File refs:** `<root>/res/drawable/APKTOOL_RENAMED_0x7f080033.xml` (& 23 others).

---

## 8. Animations

### 8a. Window animations (`<root>/res/anim/`, 42 files)

Examples:

| Pattern | Duration | Interpolator | What it is (inferred) |
|---|---|---|---|
| scale 0.9→1.0 + alpha 0→1, pivotX=50%, pivotY=100% | `@integer/u` = **220 ms** | decelerate | Bottom-anchored sheet open. |
| alpha 0→1 only | `@integer/0x7f0c0001` = **150 ms** | decelerate | Fade-in. |
| translate fromY=20% + alpha 0→1 | `@integer/0x7f0c0006` = **150 ms** | `fast_out_linear_in` | Slide-up enter. |
| translate fromY=20% + alpha 0→1 (themed) | `@integer/0x7f0c0043` (likely 250 ms) | custom interpolator `0x7f0d0007` | Themed enter. |
| translate fromX=100% → 0 | hard-coded **275 ms** | `?theme attr` | **Slide-in-from-right** — the canonical panel-open animation. |

### 8b. Property animators (`<root>/res/animator/`)

- Elevation transition between **0 dp** and `@dimen/0x7f07006e` — Material card elevation toggle.
- Standard focus/state list with `translationZ` + `elevation` cascade triggered on `state_pressed` / `state_hovered` / `state_focused`. Duration controlled by `@integer/0x7f0c005d`.
- Fade + scale + iconScale combo: 200 ms / 200 ms / 0 ms with 200 ms start-offset on iconScale — likely the FAB / button icon swap.

### 8c. Canonical durations table (from `<root>/res/values/integers.xml`)

| Hex name | Value (ms) | Likely role |
|---|---|---|
| `u` (renamed default) | **220** | Standard short animation. |
| `0x7f0c0001` | **150** | Fast fade / focus. |
| `0x7f0c0003` / `0x7f0c0012` / `0x7f0c0013` / `0x7f0c0025` / `0x7f0c0029` / `0x7f0c002d` | **250** | Medium / panel slide. |
| `0x7f0c0002` / `0x7f0c0020` | **350** | Slow / large overlay. |
| `0x7f0c0015` | **400** | Slowest content swap. |
| `0x7f0c0024` | **500** | Half-second emphasis. |
| `0x7f0c0027` | **3000** | Auto-hide timeout (likely OSD). |
| `0x7f0c000a` | **300** | Standard MD motion. |
| `0x7f0c000b` / `0x7f0c0021` / `0x7f0c0022` | **33** | Single-frame tick (~30 fps). |
| `0x7f0c0007` | **127** | Snap. |
| (literal in anim XML) | **275** | Right-panel slide. |

→ Implementation: use **220 ms** as the base motion duration, **150 ms** for focus, **275 ms** for the live-panel slide-in-from-left, **3000 ms** auto-hide for the bottom OSD.

**File refs:** `<root>/res/anim/`, `<root>/res/animator/`, `<root>/res/values/integers.xml`.

---

## 9. Manifest — Confirmed Facts

From `<root>/AndroidManifest.xml` and `<root>/apktool.yml`:

| Spec | Value |
|---|---|
| `package` | `ar.tvplayer.tv` |
| `versionName` / `versionCode` | `5.2.0` / `5208` |
| `compileSdkVersion` | **36** (Android 16) |
| `minSdkVersion` | **23** (Android 6.0 Marshmallow) |
| `targetSdkVersion` | **34** (Android 14) |
| `android:banner` | `@mipmap/banner` → `<root>/res/mipmap-xhdpi/banner.png` = **320×180 PNG, 8-bit RGB** |
| `android:icon` / `roundIcon` | `@mipmap/ic_launcher` / `@mipmap/ic_launcher_round` |
| `largeHeap` | `true` |
| `usesCleartextTraffic` | `true` (IPTV streams often plain HTTP) |
| `supportsRtl` | `false` (interesting — explicitly disabled) |
| `MainActivity` orientation | `landscape` |
| `MainActivity launchMode` | `singleTop` |
| `supportsPictureInPicture` | `true` on MainActivity |
| Custom `appComponentFactory` | `ar.tvplayer.tv.ProtectedAppComponentFactory` (DexProtector wrapper) |

### 9a. Launcher intent filter (the canonical Android-TV combo)
```
<action  android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />
<category android:name="android.intent.category.LEANBACK_LAUNCHER" />
```
No `android.intent.category.HOME` and no Fire-TV-specific category — TiViMate ships **one APK** that targets Android TV (Leanback) and relies on Fire TV's compatibility layer.

### 9b. `<uses-feature>` (compatibility-mode declarations)

| Feature | required |
|---|---|
| `android.hardware.touchscreen` | **false** (TV-friendly) |
| `android.software.leanback` | **false** (works on non-leanback handsets too) |
| `android.hardware.microphone` | **false** |

### 9c. `<uses-configuration>`
`<uses-configuration android:reqNavigation="dpad" />` — TiViMate explicitly advertises D-pad usage to launchers.

### 9d. Notable permissions
`INTERNET`, `ACCESS_NETWORK_STATE`, `MANAGE_EXTERNAL_STORAGE`, `READ/WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES/VIDEO/AUDIO`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `REQUEST_INSTALL_PACKAGES`, `RECORD_AUDIO`, `SYSTEM_ALERT_WINDOW`, `SCHEDULE_EXACT_ALARM`, `WAKE_LOCK`, `com.android.vending.BILLING`, plus an internal signature permission `ar.tvplayer.tv.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

### 9e. Notable activities (semantic names retained — TiViMate package names *were not* obfuscated)
`ar.tvplayer.tv.ui.MainActivity` (landscape, singleTop, PiP), `reminders.ui.ReminderPopupActivity`, `settings.ui.general.SelectBackupActivity` / `RestoreBackupActivity`, `settings.ui.playlists.wizard.PlaylistWizardActivity`, `settings.ui.playlists.PlaylistNameActivity` / `DeletePlaylistActivity`, `settings.ui.tvguide.TvGuideUrlActivity` / `TvGuideSourceNameActivity` / `DeleteTvGuideSourceActivity`, `settings.ui.appearance.LogosFolderActivity`, `settings.ui.other.RecordingsFolderActivity`, `settings.ui.groupoptions.ResetPositionsActivity`, `tvguide.ui.CreateChannelGroupActivity` / `CopyChannelsActivity`, `unlock.ui.UnlockPremiumActivity`, `base.ui.filepicker.FilePickerActivity` / `SmbActivity`.

> These names confirm the app's **information architecture**: `playlists / tvguide (EPG) / appearance / recordings / channel-groups / unlock-premium / file-picker (with SMB!)` are first-class settings sections. Afterglow TV should mirror this taxonomy.

### 9f. Notable services / receivers
`androidx.work.impl.foreground.SystemForegroundService` (`dataSync|mediaPlayback`), `ar.tvplayer.tv.base.ScreenOnService` (`dataSync`), `ar.tvplayer.tv.base.BootReceiver` (BOOT_COMPLETED + QUICKBOOT_POWERON), `ar.tvplayer.core.data.repositories.ReminderAlarmReceiver`, `ar.tvplayer.core.domain2.RecordingAlarmReceiver`, `ar.tvplayer.core.domain2.ScheduleExactAlarmPermissionReceiver`.

**File refs:** `<root>/AndroidManifest.xml`, `<root>/apktool.yml`.

---

## 10. Notes from Pre-Digested Files

`<root>/../branding.txt` (723k lines), `<root>/../useful_features.txt` (151k lines), `<root>/../player_logic.txt` (7.9k lines) were inspected. **They are not curated digests** — they are raw `grep -n` concordances of every source/resource file in both `tivimate_decoded/` and `tivimate_java/`, sorted by some keyword. They surface no information beyond what is already covered above, and are not skim-able for design tokens. The implementing engineer can safely ignore them and work from this cheat sheet plus the manifest activity list (§9e) for IA.

**File refs:** `C:\Users\Corey\.decompiled\tivimate\branding.txt`, `useful_features.txt`, `player_logic.txt`.

---

## Appendix — Recommended Afterglow TV Token Set (derived)

```text
// Surface
SurfaceBase       = #131313    // channel-list / panel base
SurfaceElevated   = #1c232c    // overlay column 2 (channels) on dark navy theme
SurfaceDeep       = #060606    // root background
ScrimPanel        = #c0000000  // 75% black under panels
ScrimOsd          = #80000000  // 50% black under bottom OSD

// Brand / focus
AccentPrimary     = #2196f3    // focus ring / progress bar
AccentSecondary   = #90caf9    // hover / secondary focus
NowLineRed        = #f44336    // EPG "now" vertical line (use 2 dp width)

// Text
TextPrimary       = #ffffff
TextSecondary     = #b3eeeeee  // 70 %
TextDisabled      = #4deeeeee  // 30 %

// Geometry
Unit              = 16.dp       // grid base
RowHeight         = 56.dp
RowPaddingH       = 8.dp
CornerRadius      = 4.dp
FocusStroke       = 4.dp
NowLineWidth      = 2.dp
TabHeight         = 52.dp
NavRailWidth      = 120.dp
OverlayColumn     = 320.dp      // each of the two columns
EpgSlotWidth      = 270.dp      // 30-min program slot
BottomOsdHeight   = 180.dp

// Typography (Roboto Medium)
TextDisplay       = 34.sp
TextHero          = 22.sp
TextHeader        = 16.sp
TextBody          = 14.sp
TextCaption       = 12.sp

// Motion
DurFast           = 150.ms      // focus / fade
DurStandard       = 220.ms
DurMedium         = 250.ms
DurPanel          = 275.ms      // slide-in-from-side
DurSlow           = 400.ms
OsdAutoHide       = 3000.ms
```

Every literal above is taken directly from the §1–§9 tables. The role mapping (NowLineRed, OverlayColumn etc.) is engineer's judgement — verify by eye against running TiViMate before committing them as final Afterglow TV tokens.
