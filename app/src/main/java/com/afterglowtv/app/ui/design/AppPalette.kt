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

        /** Warm sunset: deep maroon darks, amber-orange accent, gold now-line. Cozy living-room vibe. */
        val SunsetAurora = AppPalette(
            id = "sunset_aurora",
            displayName = "Sunset Aurora",
            description = "Warm maroon darks with amber-orange accent. Cozy for evening watching.",
            surfaceDeep = Color(0xFF160A12),
            surfaceBase = Color(0xFF22121A),
            surfaceCool = Color(0xFF301B26),
            surfaceAccent = Color(0xFF402533),
            accent = Color(0xFFFF8A4C),
            accentLight = Color(0xFFFFC799),
            accentMuted = Color(0x66FF8A4C),
            panelScrim = Color(0xCC1A0A14),
            osdScrim = Color(0x991A0A14),
            nowLine = Color(0xFFFFC83D),
            nowFill = Color(0x33FF8A4C),
            live = Color(0xFFFF5C5C),
            pipPreviewOutline = Color(0xFFFF8A4C),
            focusFill = Color(0x33FF8A4C),
            textPrimary = Color(0xFFFFF7ED),
            textSecondary = Color(0xCCFFE8D6),
            textTertiary = Color(0x99FFE8D6),
            textDisabled = Color(0x66FFE8D6),
            success = Color(0xFFFFC83D),
            warning = Color(0xFFFFAA00),
            info = Color(0xFFFFC799),
            divider = Color(0x1AFFE8D6),
            outline = Color(0x33FF8A4C),
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

        /** All available presets, in display order. */
        val ALL: List<AppPalette> = listOf(
            Vaporwave,
            AfterglowGray,
            Synthwave,
            NeonDusk,
            CyberPunk,
            SunsetAurora,
            ForestMist,
            PureOnyx,
            TiviMateClassic,
        )

        fun byId(id: String?): AppPalette = ALL.firstOrNull { it.id == id } ?: Vaporwave
    }
}
