package com.afterglowtv.app.ui.design

import androidx.compose.ui.graphics.Color

/**
 * A bundled theme identity: backgrounds, accents, semantics, text — everything
 * that gives AfterglowTV a distinct visual character.
 *
 * Themes are intentionally **not just hex swaps**: they pair backgrounds with
 * accents that have a coherent mood (moody-violet + mint-teal; pitch-black +
 * cyan-electric; warm-maroon + amber; etc.) and shift the `nowLine` and `live`
 * indicator hue together so the EPG still pops without clashing.
 *
 * Future work: extend with corner-radius scale, density factor, focus-highlight
 * style enum, and typography weight so themes diverge in dimension/density too.
 */
data class AppPalette(
    val id: String,
    val displayName: String,
    val description: String,

    // Surfaces — darkest → lightest
    val surfaceDeep: Color,
    val surfaceBase: Color,
    val surfaceCool: Color,
    val surfaceAccent: Color,

    // Accent family
    val accent: Color,
    val accentLight: Color,
    val accentMuted: Color,

    // Scrims
    val panelScrim: Color,
    val osdScrim: Color,

    // EPG / live indicator
    val nowLine: Color,
    val nowFill: Color,
    val live: Color,

    // PiP / framing
    val pipPreviewOutline: Color,
    val focusFill: Color,

    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // Semantics
    val success: Color,
    val warning: Color,
    val info: Color,

    // Lines
    val divider: Color,
    val outline: Color,
) {
    companion object {
        /** Default. Deep-violet darks, mint-teal accent, hot-magenta now-line. Modern, neon-leaning. */
        val NeonDusk = AppPalette(
            id = "neon_dusk",
            displayName = "Neon Dusk",
            description = "Violet darks + mint-teal accent + magenta now-line. The signature AfterglowTV look.",
            surfaceDeep = Color(0xFF0A0E16),
            surfaceBase = Color(0xFF141A24),
            surfaceCool = Color(0xFF1F2735),
            surfaceAccent = Color(0xFF2A3346),
            accent = Color(0xFF5EEAD4),
            accentLight = Color(0xFFA7F3E5),
            accentMuted = Color(0x665EEAD4),
            panelScrim = Color(0xCC080B12),
            osdScrim = Color(0x99080B12),
            nowLine = Color(0xFFFF3D7F),
            nowFill = Color(0x335EEAD4),
            live = Color(0xFFFF3D7F),
            pipPreviewOutline = Color(0xFF5EEAD4),
            focusFill = Color(0x335EEAD4),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xB3EEEEEE),
            textTertiary = Color(0x80EEEEEE),
            textDisabled = Color(0x4DEEEEEE),
            success = Color(0xFF5EEAD4),
            warning = Color(0xFFFFD166),
            info = Color(0xFFA7F3E5),
            divider = Color(0x1AFFFFFF),
            outline = Color(0x335EEAD4),
        )

        /** Pure black OLED-friendly with cyan accent and electric-pink now-line. Cyberpunk edge. */
        val CyberPunk = AppPalette(
            id = "cyber_punk",
            displayName = "Cyber Punk",
            description = "Pitch black with electric cyan and hot pink. Maximum contrast for OLED panels.",
            surfaceDeep = Color(0xFF000000),
            surfaceBase = Color(0xFF0A0A0A),
            surfaceCool = Color(0xFF111111),
            surfaceAccent = Color(0xFF1A1A1A),
            accent = Color(0xFF00E5FF),
            accentLight = Color(0xFF80F0FF),
            accentMuted = Color(0x6600E5FF),
            panelScrim = Color(0xE6000000),
            osdScrim = Color(0xB3000000),
            nowLine = Color(0xFFFF00B3),
            nowFill = Color(0x3300E5FF),
            live = Color(0xFFFF00B3),
            pipPreviewOutline = Color(0xFFFF00B3),
            focusFill = Color(0x3300E5FF),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xCCFFFFFF),
            textTertiary = Color(0x99FFFFFF),
            textDisabled = Color(0x66FFFFFF),
            success = Color(0xFF00E5FF),
            warning = Color(0xFFFFAA00),
            info = Color(0xFFFF00B3),
            divider = Color(0x2200E5FF),
            outline = Color(0x4400E5FF),
        )

        /** Rachel's Sunset — Afterglow Dark 2 DNA with a seafoam / mint-teal tide,
         *  peach sun, hot pink edge light, and orange warmth. */
        val SunsetAurora = AppPalette(
            id = "sunset_aurora",
            displayName = "Rachel's Sunset",
            description = "I love you Rachel! ❤️",
            surfaceDeep = Color(0xFF0B0714),
            surfaceBase = Color(0xFF1A0B22),
            surfaceCool = Color(0xFF2B1630),
            surfaceAccent = Color(0xFF493053),
            accent = Color(0xFF6BE2C5),
            accentLight = Color(0xFFFFB07A),
            accentMuted = Color(0x666BE2C5),
            panelScrim = Color(0xD6080610),
            osdScrim = Color(0xA6080610),
            nowLine = Color(0xFFFF5F9F),
            nowFill = Color(0x336BE2C5),
            live = Color(0xFFFF7A3D),
            pipPreviewOutline = Color(0xFF6BE2C5),
            focusFill = Color(0x406BE2C5),
            textPrimary = Color(0xFFFFF4EA),
            textSecondary = Color(0xD9F6D7D2),
            textTertiary = Color(0x99F6D7D2),
            textDisabled = Color(0x66F6D7D2),
            success = Color(0xFF6BE2C5),
            warning = Color(0xFFFFB07A),
            info = Color(0xFFFF8CB8),
            divider = Color(0x24FFE2D0),
            outline = Color(0x5577E8CF),
        )

        /** Calm forest: deep teal-black with sage-green accent and amber now-line. Easy on the eyes. */
        val ForestMist = AppPalette(
            id = "forest_mist",
            displayName = "Forest Mist",
            description = "Deep teal-black with sage-green accent. Easy on the eyes for long sessions.",
            surfaceDeep = Color(0xFF0A1310),
            surfaceBase = Color(0xFF101F1A),
            surfaceCool = Color(0xFF182B25),
            surfaceAccent = Color(0xFF223830),
            accent = Color(0xFF8FCFA1),
            accentLight = Color(0xFFC2E7CC),
            accentMuted = Color(0x668FCFA1),
            panelScrim = Color(0xCC0A1310),
            osdScrim = Color(0x990A1310),
            nowLine = Color(0xFFFFD166),
            nowFill = Color(0x338FCFA1),
            live = Color(0xFFFFD166),
            pipPreviewOutline = Color(0xFF8FCFA1),
            focusFill = Color(0x338FCFA1),
            textPrimary = Color(0xFFF0FAF4),
            textSecondary = Color(0xCCE3F0E7),
            textTertiary = Color(0x99E3F0E7),
            textDisabled = Color(0x66E3F0E7),
            success = Color(0xFF8FCFA1),
            warning = Color(0xFFFFD166),
            info = Color(0xFFC2E7CC),
            divider = Color(0x1AE3F0E7),
            outline = Color(0x338FCFA1),
        )

        /** Minimal mono: near-black + clean whites + grey accents. For minimalists. */
        val PureOnyx = AppPalette(
            id = "pure_onyx",
            displayName = "Pure Onyx",
            description = "Near-black with clean whites and minimal grey accents. Minimalist, content-first.",
            surfaceDeep = Color(0xFF050505),
            surfaceBase = Color(0xFF0F0F0F),
            surfaceCool = Color(0xFF1A1A1A),
            surfaceAccent = Color(0xFF252525),
            accent = Color(0xFFE8E8E8),
            accentLight = Color(0xFFFFFFFF),
            accentMuted = Color(0x66E8E8E8),
            panelScrim = Color(0xE6000000),
            osdScrim = Color(0xB3000000),
            nowLine = Color(0xFFE5484D),
            nowFill = Color(0x33FFFFFF),
            live = Color(0xFFE5484D),
            pipPreviewOutline = Color(0xFFE8E8E8),
            focusFill = Color(0x33FFFFFF),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xCCFFFFFF),
            textTertiary = Color(0x99FFFFFF),
            textDisabled = Color(0x66FFFFFF),
            success = Color(0xFF8FCFA1),
            warning = Color(0xFFFFD166),
            info = Color(0xFFE8E8E8),
            divider = Color(0x1AFFFFFF),
            outline = Color(0x33FFFFFF),
        )

        /** Reference: TiViMate's actual decompiled v5.2.0 palette. Homage option. */
        val TiviMateClassic = AppPalette(
            id = "tivimate_classic",
            displayName = "TiViMate Classic",
            description = "Decompiled-from-source TiViMate v5.2.0 palette. For users who liked the original.",
            surfaceDeep = Color(0xFF060606),
            surfaceBase = Color(0xFF131313),
            surfaceCool = Color(0xFF1C232C),
            surfaceAccent = Color(0xFF2C333C),
            accent = Color(0xFF2196F3),
            accentLight = Color(0xFF90CAF9),
            accentMuted = Color(0x662196F3),
            panelScrim = Color(0xC0000000),
            osdScrim = Color(0x80000000),
            nowLine = Color(0xFFF44336),
            nowFill = Color(0x602196F3),
            live = Color(0xFFF44336),
            pipPreviewOutline = Color(0xFF2C333C),
            focusFill = Color(0x402196F3),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xB3EEEEEE),
            textTertiary = Color(0x80EEEEEE),
            textDisabled = Color(0x4DEEEEEE),
            success = Color(0xFF4FD39A),
            warning = Color(0xFFFFC107),
            info = Color(0xFF2DAAE2),
            divider = Color(0x1AF4F8FF),
            outline = Color(0x264C6D95),
        )

        /** Synthwave — deep indigo + hot magenta + neon cyan + sun-yellow now-line. 80s sunset grid energy. */
        val Synthwave = AppPalette(
            id = "synthwave",
            displayName = "Synthwave",
            description = "Deep indigo + hot magenta + neon cyan. 80s sunset-grid energy with sun-yellow now-line.",
            surfaceDeep = Color(0xFF0F0524),
            surfaceBase = Color(0xFF1A0B33),
            surfaceCool = Color(0xFF261144),
            surfaceAccent = Color(0xFF351C5C),
            accent = Color(0xFFFF2A6D),
            accentLight = Color(0xFFFF6EA0),
            accentMuted = Color(0x66FF2A6D),
            panelScrim = Color(0xCC0A0322),
            osdScrim = Color(0x990A0322),
            nowLine = Color(0xFFFFD000),
            nowFill = Color(0x3300F0FF),
            live = Color(0xFFFFD000),
            pipPreviewOutline = Color(0xFF00F0FF),
            focusFill = Color(0x3300F0FF),
            textPrimary = Color(0xFFFFE9F8),
            textSecondary = Color(0xCCFFCFE5),
            textTertiary = Color(0x99FFCFE5),
            textDisabled = Color(0x66FFCFE5),
            success = Color(0xFF00F0FF),
            warning = Color(0xFFFFD000),
            info = Color(0xFFFF6EA0),
            divider = Color(0x22FF6EA0),
            outline = Color(0x4400F0FF),
        )

        /** Vaporwave — deep aubergine purple + neon sunset orange + porange (hot pink-orange).
         *  A melting-sun palette: dusk-purple skies, sun-core gold, sun-edge porange.
         *  Replaces the previous pastel-cyan vaporwave per design feedback. */
        val Vaporwave = AppPalette(
            id = "vaporwave",
            displayName = "Vaporwave",
            description = "Deep purple skies with a melting neon sun — sunset orange + porange (pink+orange).",
            surfaceDeep = Color(0xFF160427),         // deep aubergine
            surfaceBase = Color(0xFF2C0C4A),         // rich purple
            surfaceCool = Color(0xFF3D165F),         // mid purple
            surfaceAccent = Color(0xFF5A2484),       // lavender purple
            accent = Color(0xFFFF7A38),              // neon sunset orange — the dominant melting-sun color
            accentLight = Color(0xFFFFC44A),         // sun-core gold (melting sun center)
            accentMuted = Color(0x66FF7A38),         // 40% sunset orange
            panelScrim = Color(0xCC120324),
            osdScrim = Color(0x99120324),
            nowLine = Color(0xFFFF3DAF),             // hot porange — pink+orange neon
            nowFill = Color(0x33FF7A38),             // 20% sunset orange
            live = Color(0xFFFF3DAF),                // porange for LIVE pulse
            pipPreviewOutline = Color(0xFFFF477E),   // slightly pinker porange for the PiP frame
            focusFill = Color(0x40FF7A38),           // 25% sunset orange
            textPrimary = Color(0xFFFFF7E8),         // warm cream
            textSecondary = Color(0xCCFFE8C7),       // dusty cream
            textTertiary = Color(0x99FFE8C7),
            textDisabled = Color(0x66FFE8C7),
            success = Color(0xFFFFC44A),             // sun gold doubles as success
            warning = Color(0xFFFFC44A),
            info = Color(0xFFFFB47A),                // pale sunset
            divider = Color(0x22FFE8C7),
            outline = Color(0x40FF7A38),
        )

        /** Afterglow Gray — direct port of the user's favorite YTAfterglow preset.
         *  Charcoal-monochrome: pure grayscale luminance steps, no chroma. Pure
         *  white accent on `#2E2E2E` body. The Lite-inspired "soft white controls
         *  on charcoal" look from the iOS YouTube tweak, applied to a TV grid. */
        val AfterglowGray = AppPalette(
            id = "afterglow_gray",
            displayName = "Afterglow Gray",
            description = "Charcoal monochrome — pure white controls on `#2E2E2E` body. The Lite-inspired YT favorite.",
            surfaceDeep = Color(0xFF2E2E2E),         // YTAG bg (white 0.18)
            surfaceBase = Color(0xFF3D3D3D),         // YTAG nav (white 0.24)
            surfaceCool = Color(0xFF4A4A4A),         // extrapolated step
            surfaceAccent = Color(0xFF575757),       // extrapolated step
            accent = Color(0xFFFFFFFF),              // YTAG accent — pure white
            accentLight = Color(0xFFFAFAFA),         // YTAG overlay
            accentMuted = Color(0x66FFFFFF),
            panelScrim = Color(0xCC1F1F1F),
            osdScrim = Color(0x991F1F1F),
            nowLine = Color(0xFFFFFFFF),             // pure-white now-line pops on `#2E2E2E`
            nowFill = Color(0x33FFFFFF),
            live = Color(0xFFFFFFFF),
            pipPreviewOutline = Color(0xFFFFFFFF),
            focusFill = Color(0x33FFFFFF),
            textPrimary = Color(0xFFF5F5F5),         // YTAG textP (white 0.96)
            textSecondary = Color(0xFFC7C7C7),       // YTAG textS (white 0.78)
            textTertiary = Color(0x99F5F5F5),
            textDisabled = Color(0x66F5F5F5),
            success = Color(0xFFFFFFFF),             // monochrome keeps semantics in luminance only
            warning = Color(0xFFC7C7C7),
            info = Color(0xFFEBEBEB),                // YTAG seekBar
            divider = Color(0x22FFFFFF),
            outline = Color(0x33FFFFFF),
        )

        /** Afterglow Dark 2 — default first-run palette.
         *  Deep violet surfaces with neon orange focus, hot-pink live/now-line,
         *  and peach highlight states. */
        val AfterglowSunset = AppPalette(
            id = "afterglow_sunset",
            displayName = "Afterglow Dark 2",
            description = "Deep violet with neon orange and hot-pink Afterglow accents.",
            surfaceDeep = Color(0xFF07030F),
            surfaceBase = Color(0xFF150A22),
            surfaceCool = Color(0xFF2A183E),
            surfaceAccent = Color(0xFF513A6B),
            accent = Color(0xFFFF7A18),
            accentLight = Color(0xFFFFB15F),
            accentMuted = Color(0x66FF7A18),
            panelScrim = Color(0xCC0D0311),
            osdScrim = Color(0x990D0311),
            nowLine = Color(0xFFFF2D7A),
            nowFill = Color(0x33FF7A18),
            live = Color(0xFFFF2D7A),
            pipPreviewOutline = Color(0xFFFF7A18),
            focusFill = Color(0x40FF7A18),
            textPrimary = Color(0xFFFFF7F0),
            textSecondary = Color(0xCCFFE4D8),
            textTertiary = Color(0x99FFE4D8),
            textDisabled = Color(0x66FFE4D8),
            success = Color(0xFFFFB15F),
            warning = Color(0xFFFF7A18),
            info = Color(0xFFFF7DB0),
            divider = Color(0x22FFE4D8),
            outline = Color(0x55FF7A18),
        )

        /** Afterglow 1 — violet ember glass. Warmer and darker than the old cyan set. */
        val Afterglow1 = AppPalette(
            id = "afterglow_1",
            displayName = "Afterglow Dark 1",
            description = "Blackberry violet with ember-orange focus and rose-pink live glow.",
            surfaceDeep = Color(0xFF090613),
            surfaceBase = Color(0xFF171021),
            surfaceCool = Color(0xFF24172F),
            surfaceAccent = Color(0xFF38244A),
            accent = Color(0xFFFF8A3D),
            accentLight = Color(0xFFFFB06E),
            accentMuted = Color(0x66FF8A3D),
            panelScrim = Color(0xD6070410),
            osdScrim = Color(0x99070410),
            nowLine = Color(0xFFFF4E92),
            nowFill = Color(0x33FF8A3D),
            live = Color(0xFFFF4E92),
            pipPreviewOutline = Color(0xFFFF8A3D),
            focusFill = Color(0x40FF8A3D),
            textPrimary = Color(0xFFFFF2E8),
            textSecondary = Color(0xD9F4CCBE),
            textTertiary = Color(0x99F4CCBE),
            textDisabled = Color(0x66F4CCBE),
            success = Color(0xFF6EDDC6),
            warning = Color(0xFFFFB06E),
            info = Color(0xFFFF83B6),
            divider = Color(0x22F4CCBE),
            outline = Color(0x55FF8A3D),
        )

        /** Afterglow 3 — midnight coral, dark TV-room contrast with soft teal air. */
        val Afterglow3 = AppPalette(
            id = "afterglow_3",
            displayName = "Afterglow Dark 3",
            description = "Ink-violet base with coral peach controls, seafoam highlights, and pink live edge.",
            surfaceDeep = Color(0xFF070A14),
            surfaceBase = Color(0xFF101523),
            surfaceCool = Color(0xFF1B2133),
            surfaceAccent = Color(0xFF2E3149),
            accent = Color(0xFFFF9A68),
            accentLight = Color(0xFF7EE7D4),
            accentMuted = Color(0x66FF9A68),
            panelScrim = Color(0xD6040710),
            osdScrim = Color(0x99040710),
            nowLine = Color(0xFFFF5AA3),
            nowFill = Color(0x337EE7D4),
            live = Color(0xFFFF5AA3),
            pipPreviewOutline = Color(0xFF7EE7D4),
            focusFill = Color(0x40FF9A68),
            textPrimary = Color(0xFFF7F2FF),
            textSecondary = Color(0xD9D8C7E8),
            textTertiary = Color(0x99D8C7E8),
            textDisabled = Color(0x66D8C7E8),
            success = Color(0xFF7EE7D4),
            warning = Color(0xFFFFC06D),
            info = Color(0xFFFF8AB8),
            divider = Color(0x22D8C7E8),
            outline = Color(0x557EE7D4),
        )

        /** Afterglow 4 — ember noir. High contrast without the old neon-green clash. */
        val Afterglow4 = AppPalette(
            id = "afterglow_4",
            displayName = "Afterglow Dark 4",
            description = "Near-black plum with neon orange, pink flame, and a restrained mint signal color.",
            surfaceDeep = Color(0xFF050409),
            surfaceBase = Color(0xFF0E0A12),
            surfaceCool = Color(0xFF18101F),
            surfaceAccent = Color(0xFF2C1834),
            accent = Color(0xFFFF6F1F),
            accentLight = Color(0xFFFFA057),
            accentMuted = Color(0x66FF6F1F),
            panelScrim = Color(0xE6030307),
            osdScrim = Color(0xAA030307),
            nowLine = Color(0xFFFF3D87),
            nowFill = Color(0x33FF6F1F),
            live = Color(0xFFFF3D87),
            pipPreviewOutline = Color(0xFFFF6F1F),
            focusFill = Color(0x45FF6F1F),
            textPrimary = Color(0xFFFFF6EF),
            textSecondary = Color(0xD9E8D3CC),
            textTertiary = Color(0x99E8D3CC),
            textDisabled = Color(0x66E8D3CC),
            success = Color(0xFF67DABF),
            warning = Color(0xFFFFA057),
            info = Color(0xFFFF77A8),
            divider = Color(0x22E8D3CC),
            outline = Color(0x55FF6F1F),
        )

        // ── Light palettes ───────────────────────────────────────────
        // The light themes are intentionally not white themes. They use
        // smoky, sunset-tinted surfaces with dark text so TV menus keep real
        // contrast instead of turning into a bright flat sheet.

        /** Afterglow Gray Light — pale charcoal monochrome (the inverse of
         *  Afterglow Gray Dark). Bright surfaces, dark controls. */
        val AfterglowGrayLight = AppPalette(
            id = "afterglow_gray_light",
            displayName = "Afterglow Gray Light",
            description = "Soft ash monochrome — dark controls on a muted gray body.",
            surfaceDeep = Color(0xFFC9C9C9),
            surfaceBase = Color(0xFFD8D8D8),
            surfaceCool = Color(0xFFE3E3E3),
            surfaceAccent = Color(0xFFB7B7B7),
            accent = Color(0xFF202020),
            accentLight = Color(0xFF464646),
            accentMuted = Color(0x66333333),
            panelScrim = Color(0xCC1F1F1F),
            osdScrim = Color(0x991F1F1F),
            nowLine = Color(0xFF2C2C2C),
            nowFill = Color(0x33333333),
            live = Color(0xFF202020),
            pipPreviewOutline = Color(0xFF202020),
            focusFill = Color(0x33333333),
            textPrimary = Color(0xFF111111),
            textSecondary = Color(0xFF3D3D3D),
            textTertiary = Color(0x993D3D3D),
            textDisabled = Color(0x663D3D3D),
            success = Color(0xFF202020),
            warning = Color(0xFF4A4A4A),
            info = Color(0xFF2C2C2C),
            divider = Color(0x22000000),
            outline = Color(0x33333333),
        )

        /** Afterglow Light 1 — smoky lilac, orange-pink glow, no whiteout. */
        val AfterglowLight1 = AppPalette(
            id = "afterglow_light_1",
            displayName = "Afterglow Light 1",
            description = "Smoky lilac glass with orange controls, pink live glow, and mint highlights.",
            surfaceDeep = Color(0xFFD7C5D6),
            surfaceBase = Color(0xFFE3D5E1),
            surfaceCool = Color(0xFFECE2EA),
            surfaceAccent = Color(0xFFC8A9C6),
            accent = Color(0xFF9A3B1D),
            accentLight = Color(0xFF1E6F63),
            accentMuted = Color(0x669A3B1D),
            panelScrim = Color(0xCC24182B),
            osdScrim = Color(0x9924182B),
            nowLine = Color(0xFFB82362),
            nowFill = Color(0x339A3B1D),
            live = Color(0xFFB82362),
            pipPreviewOutline = Color(0xFF9A3B1D),
            focusFill = Color(0x339A3B1D),
            textPrimary = Color(0xFF211827),
            textSecondary = Color(0xFF4B344E),
            textTertiary = Color(0x994B344E),
            textDisabled = Color(0x664B344E),
            success = Color(0xFF1E6F63),
            warning = Color(0xFF9A3B1D),
            info = Color(0xFF7A2754),
            divider = Color(0x22211827),
            outline = Color(0x409A3B1D),
        )

        /** Afterglow Light 2 — dim apricot sunset, closest light cousin to Dark 2. */
        val AfterglowLight2 = AppPalette(
            id = "afterglow_light_2",
            displayName = "Afterglow Light 2",
            description = "Dim apricot and mauve surfaces with dark violet text, orange focus, and pink live glow.",
            surfaceDeep = Color(0xFFD8B59E),
            surfaceBase = Color(0xFFE5C9B6),
            surfaceCool = Color(0xFFEED8CB),
            surfaceAccent = Color(0xFFBA836B),
            accent = Color(0xFF8B3419),
            accentLight = Color(0xFF5B245F),
            accentMuted = Color(0x668B3419),
            panelScrim = Color(0xCC1A0B22),
            osdScrim = Color(0x991A0B22),
            nowLine = Color(0xFFB5265E),
            nowFill = Color(0x338B3419),
            live = Color(0xFFB5265E),
            pipPreviewOutline = Color(0xFF8B3419),
            focusFill = Color(0x408B3419),
            textPrimary = Color(0xFF211120),
            textSecondary = Color(0xFF553448),
            textTertiary = Color(0x99553448),
            textDisabled = Color(0x66553448),
            success = Color(0xFF236E5F),
            warning = Color(0xFF8B3419),
            info = Color(0xFF7B2A64),
            divider = Color(0x22211120),
            outline = Color(0x408B3419),
        )

        /** Afterglow Light 3 — muted seafoam with violet-orange contrast. */
        val AfterglowLight3 = AppPalette(
            id = "afterglow_light_3",
            displayName = "Afterglow Light 3",
            description = "Muted seafoam and slate-mint panels with dark teal text, violet depth, and peach focus.",
            surfaceDeep = Color(0xFFBFD8D1),
            surfaceBase = Color(0xFFD0E2DC),
            surfaceCool = Color(0xFFDDEBE6),
            surfaceAccent = Color(0xFF95BFB4),
            accent = Color(0xFF7E3B18),
            accentLight = Color(0xFF4B2B77),
            accentMuted = Color(0x667E3B18),
            panelScrim = Color(0xCC102B27),
            osdScrim = Color(0x99102B27),
            nowLine = Color(0xFF9F285D),
            nowFill = Color(0x337E3B18),
            live = Color(0xFF9F285D),
            pipPreviewOutline = Color(0xFF4B2B77),
            focusFill = Color(0x407E3B18),
            textPrimary = Color(0xFF102B27),
            textSecondary = Color(0xFF294D47),
            textTertiary = Color(0x99294D47),
            textDisabled = Color(0x66294D47),
            success = Color(0xFF1B665B),
            warning = Color(0xFF7E3B18),
            info = Color(0xFF4B2B77),
            divider = Color(0x22102B27),
            outline = Color(0x404B2B77),
        )

        /** Rachel's Sunset Light — daylight counterpart to the dedicated
         *  Rachel's Sunset palette. It stays warm and pretty without blasting
         *  the screen white: peach-rose surfaces, mint-teal focus, plum text. */
        val RachelsSunsetLight = AppPalette(
            id = "rachels_sunset_light",
            displayName = "Rachel's Sunset Light",
            description = "I love you Rachel! ❤️ — daylight version with mint teal.",
            surfaceDeep = Color(0xFFD9B8AE),
            surfaceBase = Color(0xFFE7CBC0),
            surfaceCool = Color(0xFFF0D9D0),
            surfaceAccent = Color(0xFFB98983),
            accent = Color(0xFF156F63),
            accentLight = Color(0xFFA64225),
            accentMuted = Color(0x66156F63),
            panelScrim = Color(0xCC1A0B22),
            osdScrim = Color(0x991A0B22),
            nowLine = Color(0xFF9E2759),
            nowFill = Color(0x33156F63),
            live = Color(0xFFA64225),
            pipPreviewOutline = Color(0xFF156F63),
            focusFill = Color(0x40156F63),
            textPrimary = Color(0xFF241326),
            textSecondary = Color(0xFF57324A),
            textTertiary = Color(0x9957324A),
            textDisabled = Color(0x6657324A),
            success = Color(0xFF156F63),
            warning = Color(0xFFA64225),
            info = Color(0xFF8E2E62),
            divider = Color(0x22241326),
            outline = Color(0x40156F63),
        )

        /** Afterglow Light 4 — rose ash, peach controls, mint secondary. */
        val AfterglowLight4 = AppPalette(
            id = "afterglow_light_4",
            displayName = "Afterglow Light 4",
            description = "Dusty rose-ash panels with burnt peach focus, mint signal, and plum contrast.",
            surfaceDeep = Color(0xFFD4BEC4),
            surfaceBase = Color(0xFFE0CED2),
            surfaceCool = Color(0xFFEADDE0),
            surfaceAccent = Color(0xFFB8949D),
            accent = Color(0xFF8F3518),
            accentLight = Color(0xFF176B63),
            accentMuted = Color(0x668F3518),
            panelScrim = Color(0xCC1C0D1B),
            osdScrim = Color(0x991C0D1B),
            nowLine = Color(0xFFA6225A),
            nowFill = Color(0x338F3518),
            live = Color(0xFFA6225A),
            pipPreviewOutline = Color(0xFF176B63),
            focusFill = Color(0x408F3518),
            textPrimary = Color(0xFF1C0D1B),
            textSecondary = Color(0xFF4D3046),
            textTertiary = Color(0x994D3046),
            textDisabled = Color(0x664D3046),
            success = Color(0xFF176B63),
            warning = Color(0xFF8F3518),
            info = Color(0xFF7E2553),
            divider = Color(0x221C0D1B),
            outline = Color(0x40176B63),
        )

        /** All available presets, in display order. Afterglow Dark 1-4 first
         *  in numerical order (Dark 2 is the default), then the gray
         *  variant, then Afterglow Light 1-4, then the gray light, then the
         *  generic-named bundles last. */
        val ALL: List<AppPalette> = listOf(
            Afterglow1,           // Afterglow Dark 1
            AfterglowSunset,      // Afterglow Dark 2 — default
            Afterglow3,           // Afterglow Dark 3
            Afterglow4,           // Afterglow Dark 4
            AfterglowGray,        // Afterglow Gray (dark monochrome)
            SunsetAurora,         // Rachel's Sunset (dark dedication palette)
            AfterglowLight1,
            AfterglowLight2,
            AfterglowLight3,
            AfterglowLight4,
            AfterglowGrayLight,
            RachelsSunsetLight,   // Rachel's Sunset Light (mint teal)
            Vaporwave,
            Synthwave,
            NeonDusk,
            CyberPunk,
            ForestMist,
            PureOnyx,
            TiviMateClassic,
        )

        fun byId(id: String?): AppPalette = ALL.firstOrNull { it.id == id } ?: AfterglowSunset
    }
}
