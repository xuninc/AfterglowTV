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

        /** Rachel's Sunset — the user's dedication palette, redesigned 2026-05-14
         *  to be girly, bright, and built around sea-foam green + neon peach.
         *  Pink-rose surfaces (lighter than the original deep maroon), neon
         *  peach as the "sun" accent, sea foam as the fresh sky highlight,
         *  hot pink for the live pulse. */
        val SunsetAurora = AppPalette(
            id = "sunset_aurora",
            displayName = "Rachel's Sunset",
            description = "I love you Rachel! ❤️",
            surfaceDeep = Color(0xFF3D1A30),       // deep rose
            surfaceBase = Color(0xFF522440),       // rose
            surfaceCool = Color(0xFF6B3052),       // mauve
            surfaceAccent = Color(0xFF874166),     // light mauve
            accent = Color(0xFFFFAB7A),            // neon peach — the sun
            accentLight = Color(0xFF9FE8C5),       // sea foam green — sky / highlight
            accentMuted = Color(0x66FFAB7A),
            panelScrim = Color(0xCC2A0F1F),
            osdScrim = Color(0x992A0F1F),
            nowLine = Color(0xFF9FE8C5),           // sea foam now-line — fresh and unmistakable
            nowFill = Color(0x33FFAB7A),
            live = Color(0xFFFF77A8),              // hot pink live pulse
            pipPreviewOutline = Color(0xFF9FE8C5), // sea foam frame
            focusFill = Color(0x409FE8C5),
            textPrimary = Color(0xFFFFF0F5),       // lavender-blush cream
            textSecondary = Color(0xCCFFE0EC),     // pink-tinted secondary
            textTertiary = Color(0x99FFE0EC),
            textDisabled = Color(0x66FFE0EC),
            success = Color(0xFF9FE8C5),           // sea foam doubles as success
            warning = Color(0xFFFFAB7A),           // peach doubles as warning
            info = Color(0xFFC5F0DC),              // pale sea foam
            divider = Color(0x22FFE0EC),
            outline = Color(0x40FFAB7A),
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

        /** Afterglow Dark 2 — direct port of YTAfterglow's "Afterglow 2" preset.
         *  Peach fire over electric violet: deep aubergine surfaces with a
         *  bright cyan accent, hot pink live / now-line, coral peach as the
         *  secondary highlight. Default first-run palette. */
        val AfterglowSunset = AppPalette(
            id = "afterglow_sunset",
            displayName = "Afterglow Dark 2",
            description = "Peach fire over electric violet.",
            surfaceDeep = Color(0xFF1C0D21),         // YTAG bg
            surfaceBase = Color(0xFF2B1430),         // YTAG nav
            surfaceCool = Color(0xFF3D1B40),         // extrapolated step
            surfaceAccent = Color(0xFF552555),       // extrapolated step
            accent = Color(0xFF69E3FF),              // YTAG accent — electric cyan
            accentLight = Color(0xFFFF8C6B),         // YTAG tabIcons — coral peach
            accentMuted = Color(0x6669E3FF),
            panelScrim = Color(0xCC150921),
            osdScrim = Color(0x99150921),
            nowLine = Color(0xFFFF7AA8),             // YTAG seekBar — hot pink
            nowFill = Color(0x3369E3FF),
            live = Color(0xFFFF7AA8),                // hot pink pulse
            pipPreviewOutline = Color(0xFF69E3FF),   // cyan frame
            focusFill = Color(0x4069E3FF),
            textPrimary = Color(0xFFFFF0E6),         // YTAG textP — warm cream
            textSecondary = Color(0xFFC9AAB8),       // YTAG textS — dusty pink
            textTertiary = Color(0x99C9AAB8),
            textDisabled = Color(0x66C9AAB8),
            success = Color(0xFF69E3FF),
            warning = Color(0xFFFF8C6B),
            info = Color(0xFFFFD1B5),                // YTAG overlay — pale peach
            divider = Color(0x22FFE0EC),
            outline = Color(0x4069E3FF),
        )

        /** Afterglow 1 — hot magenta + cyan vaporwave. Deep indigo body,
         *  electric cyan accent, hot magenta now-line, light-pink text glow. */
        val Afterglow1 = AppPalette(
            id = "afterglow_1",
            displayName = "Afterglow Dark 1",
            description = "Hot magenta + electric cyan vaporwave on deep indigo.",
            surfaceDeep = Color(0xFF170829),
            surfaceBase = Color(0xFF210D33),
            surfaceCool = Color(0xFF2E1145),
            surfaceAccent = Color(0xFF3D1A58),
            accent = Color(0xFF4DE8FF),
            accentLight = Color(0xFF47F2FF),
            accentMuted = Color(0x664DE8FF),
            panelScrim = Color(0xCC0F051C),
            osdScrim = Color(0x990F051C),
            nowLine = Color(0xFFFF45BF),
            nowFill = Color(0x334DE8FF),
            live = Color(0xFFFF45BF),
            pipPreviewOutline = Color(0xFF4DE8FF),
            focusFill = Color(0x404DE8FF),
            textPrimary = Color(0xFFFAE5FC),
            textSecondary = Color(0xFFB89CD1),
            textTertiary = Color(0x99FAE5FC),
            textDisabled = Color(0x66FAE5FC),
            success = Color(0xFF4DE8FF),
            warning = Color(0xFFFF45BF),
            info = Color(0xFFFFB8E6),
            divider = Color(0x22FAE5FC),
            outline = Color(0x404DE8FF),
        )

        /** Afterglow 3 — cyber dusk indigo + laser cyan + chrome pink.
         *  Pink primary, cyan secondary, purple-magenta live pulse. */
        val Afterglow3 = AppPalette(
            id = "afterglow_3",
            displayName = "Afterglow Dark 3",
            description = "Cyber dusk — chrome pink primary, laser cyan, purple live pulse.",
            surfaceDeep = Color(0xFF0A0F29),
            surfaceBase = Color(0xFF121733),
            surfaceCool = Color(0xFF1A2042),
            surfaceAccent = Color(0xFF252B55),
            accent = Color(0xFFFF8AB8),
            accentLight = Color(0xFF57E6FF),
            accentMuted = Color(0x66FF8AB8),
            panelScrim = Color(0xCC07091F),
            osdScrim = Color(0x9907091F),
            nowLine = Color(0xFFEB4FFF),
            nowFill = Color(0x33FF8AB8),
            live = Color(0xFFEB4FFF),
            pipPreviewOutline = Color(0xFF57E6FF),
            focusFill = Color(0x40FF8AB8),
            textPrimary = Color(0xFFEDF2FF),
            textSecondary = Color(0xFF9EB3E3),
            textTertiary = Color(0x99EDF2FF),
            textDisabled = Color(0x66EDF2FF),
            success = Color(0xFF57E6FF),
            warning = Color(0xFFFF8AB8),
            info = Color(0xFFE3C9FF),
            divider = Color(0x22EDF2FF),
            outline = Color(0x40FF8AB8),
        )

        /** Afterglow 4 — jet-black cybergrid with neon green bloom, hot
         *  magenta sparks, electric violet chrome. The most aggressive set. */
        val Afterglow4 = AppPalette(
            id = "afterglow_4",
            displayName = "Afterglow Dark 4",
            description = "Jet-black cybergrid — neon green + hot magenta + electric violet.",
            surfaceDeep = Color(0xFF05050D),
            surfaceBase = Color(0xFF0D0D17),
            surfaceCool = Color(0xFF161624),
            surfaceAccent = Color(0xFF1F1F33),
            accent = Color(0xFF8C45FF),
            accentLight = Color(0xFF33FF78),
            accentMuted = Color(0x668C45FF),
            panelScrim = Color(0xCC030308),
            osdScrim = Color(0x99030308),
            nowLine = Color(0xFFFF33B3),
            nowFill = Color(0x338C45FF),
            live = Color(0xFFFF33B3),
            pipPreviewOutline = Color(0xFF8C45FF),
            focusFill = Color(0x408C45FF),
            textPrimary = Color(0xFFF5F5FF),
            textSecondary = Color(0xFFABA3D4),
            textTertiary = Color(0x99F5F5FF),
            textDisabled = Color(0x66F5F5FF),
            success = Color(0xFF33FF78),
            warning = Color(0xFFFF33B3),
            info = Color(0xFFFADBF2),
            divider = Color(0x22F5F5FF),
            outline = Color(0x408C45FF),
        )

        // ── Light palettes ───────────────────────────────────────────
        // YTAfterglow ships matching light variants of every Afterglow
        // theme. They invert the surface/text relationship — pale surfaces,
        // dark text, accent colors stay vivid. Glow effects work less well
        // on light backgrounds but the palettes are here for completeness.

        /** Afterglow Gray Light — pale charcoal monochrome (the inverse of
         *  Afterglow Gray Dark). Bright surfaces, dark controls. */
        val AfterglowGrayLight = AppPalette(
            id = "afterglow_gray_light",
            displayName = "Afterglow Gray Light",
            description = "Pale monochrome — dark controls on `#E6E6E6` body.",
            surfaceDeep = Color(0xFFE6E6E6),
            surfaceBase = Color(0xFFF0F0F0),
            surfaceCool = Color(0xFFF5F5F5),
            surfaceAccent = Color(0xFFDDDDDD),
            accent = Color(0xFF333333),
            accentLight = Color(0xFF5A5A5A),
            accentMuted = Color(0x66333333),
            panelScrim = Color(0xCC1F1F1F),
            osdScrim = Color(0x991F1F1F),
            nowLine = Color(0xFF474747),
            nowFill = Color(0x33333333),
            live = Color(0xFF1F1F1F),
            pipPreviewOutline = Color(0xFF333333),
            focusFill = Color(0x33333333),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF666666),
            textTertiary = Color(0x99666666),
            textDisabled = Color(0x66666666),
            success = Color(0xFF1F1F1F),
            warning = Color(0xFF474747),
            info = Color(0xFF292929),
            divider = Color(0x22000000),
            outline = Color(0x33333333),
        )

        /** Afterglow Light 1 — candyglass daylight, lilac body with aqua chrome. */
        val AfterglowLight1 = AppPalette(
            id = "afterglow_light_1",
            displayName = "Afterglow Light 1",
            description = "Candyglass lilac body with aqua chrome and hot-pink scrubber.",
            surfaceDeep = Color(0xFFF7D6FF),
            surfaceBase = Color(0xFFFCE7FF),
            surfaceCool = Color(0xFFFFEEFF),
            surfaceAccent = Color(0xFFE8B5F0),
            accent = Color(0xFF14CCD6),
            accentLight = Color(0xFF5FE0E8),
            accentMuted = Color(0x6614CCD6),
            panelScrim = Color(0xCC8C3089),
            osdScrim = Color(0x998C3089),
            nowLine = Color(0xFFFA54B0),
            nowFill = Color(0x3314CCD6),
            live = Color(0xFFFA54B0),
            pipPreviewOutline = Color(0xFF14CCD6),
            focusFill = Color(0x3314CCD6),
            textPrimary = Color(0xFF8C3089),
            textSecondary = Color(0xFF6D2569),
            textTertiary = Color(0x996D2569),
            textDisabled = Color(0x666D2569),
            success = Color(0xFF14CCD6),
            warning = Color(0xFFFA54B0),
            info = Color(0xFF5FE0E8),
            divider = Color(0x228C3089),
            outline = Color(0x3314CCD6),
        )

        /** Afterglow Light 2 — apricot sunset with coral punch and blue contrast. */
        val AfterglowLight2 = AppPalette(
            id = "afterglow_light_2",
            displayName = "Afterglow Light 2",
            description = "Apricot body with sky-blue accent, coral text, hot-pink scrubber.",
            surfaceDeep = Color(0xFFFFDBB8),
            surfaceBase = Color(0xFFFFE8CF),
            surfaceCool = Color(0xFFFFF1DE),
            surfaceAccent = Color(0xFFFCC99B),
            accent = Color(0xFF47ABFF),
            accentLight = Color(0xFF87C8FF),
            accentMuted = Color(0x6647ABFF),
            panelScrim = Color(0xCC993D24),
            osdScrim = Color(0x99993D24),
            nowLine = Color(0xFFFF4F70),
            nowFill = Color(0x3347ABFF),
            live = Color(0xFFFF4F70),
            pipPreviewOutline = Color(0xFF47ABFF),
            focusFill = Color(0x3347ABFF),
            textPrimary = Color(0xFF993D24),
            textSecondary = Color(0xFF7A3015),
            textTertiary = Color(0x997A3015),
            textDisabled = Color(0x667A3015),
            success = Color(0xFF47ABFF),
            warning = Color(0xFFFF4F70),
            info = Color(0xFF87C8FF),
            divider = Color(0x22993D24),
            outline = Color(0x3347ABFF),
        )

        /** Afterglow Light 3 — mint arcade glass, teal body with violet chrome. */
        val AfterglowLight3 = AppPalette(
            id = "afterglow_light_3",
            displayName = "Afterglow Light 3",
            description = "Mint body with electric violet accent and berry-pink scrubber.",
            surfaceDeep = Color(0xFFCCFFE8),
            surfaceBase = Color(0xFFDDFFEF),
            surfaceCool = Color(0xFFE9FFF5),
            surfaceAccent = Color(0xFFB5F0D3),
            accent = Color(0xFF684DF2),
            accentLight = Color(0xFF9586F7),
            accentMuted = Color(0x66684DF2),
            panelScrim = Color(0xCC21664F),
            osdScrim = Color(0x9921664F),
            nowLine = Color(0xFFFA5E94),
            nowFill = Color(0x33684DF2),
            live = Color(0xFFFA5E94),
            pipPreviewOutline = Color(0xFF684DF2),
            focusFill = Color(0x33684DF2),
            textPrimary = Color(0xFF21664F),
            textSecondary = Color(0xFF184A39),
            textTertiary = Color(0x99184A39),
            textDisabled = Color(0x66184A39),
            success = Color(0xFF21664F),
            warning = Color(0xFFFA5E94),
            info = Color(0xFF9586F7),
            divider = Color(0x2221664F),
            outline = Color(0x33684DF2),
        )

        /** Rachel's Sunset Light — daylight counterpart to the dedicated
         *  Rachel's Sunset palette. Pale blush surfaces, neon peach accent,
         *  mint-teal (sea foam with a blue tilt) for the live indicator and
         *  highlight. Deep mauve text for contrast on the pink body. */
        val RachelsSunsetLight = AppPalette(
            id = "rachels_sunset_light",
            displayName = "Rachel's Sunset Light",
            description = "I love you Rachel! ❤️ — daylight version with mint teal.",
            surfaceDeep = Color(0xFFFFE8F0),       // pale pink body
            surfaceBase = Color(0xFFFFF0F5),       // lavender blush
            surfaceCool = Color(0xFFFFF8FB),       // near-white pink tint
            surfaceAccent = Color(0xFFFCD5E2),     // slightly deeper pink
            accent = Color(0xFFFF9E78),            // neon peach — pops on light bg
            accentLight = Color(0xFF7FE0CC),       // mint teal — user's request
            accentMuted = Color(0x66FF9E78),
            panelScrim = Color(0xCC3D1A30),
            osdScrim = Color(0x993D1A30),
            nowLine = Color(0xFF7FE0CC),           // mint-teal now-line
            nowFill = Color(0x33FF9E78),
            live = Color(0xFFFF5589),              // hot pink pulse, deeper for light-bg readability
            pipPreviewOutline = Color(0xFF7FE0CC),
            focusFill = Color(0x407FE0CC),
            textPrimary = Color(0xFF5C1F44),       // deep mauve
            textSecondary = Color(0xFF7A3A60),
            textTertiary = Color(0x997A3A60),
            textDisabled = Color(0x667A3A60),
            success = Color(0xFF7FE0CC),
            warning = Color(0xFFFF9E78),
            info = Color(0xFFB5F0E0),              // pale mint
            divider = Color(0x225C1F44),
            outline = Color(0x407FE0CC),
        )

        /** Afterglow Light 4 — sky chrome, icy blue body with pink chrome. */
        val AfterglowLight4 = AppPalette(
            id = "afterglow_light_4",
            displayName = "Afterglow Light 4",
            description = "Icy blue body with chrome pink accent and coral scrubber.",
            surfaceDeep = Color(0xFFD1E8FF),
            surfaceBase = Color(0xFFE0EEFF),
            surfaceCool = Color(0xFFEBF4FF),
            surfaceAccent = Color(0xFFBAD8FA),
            accent = Color(0xFFD650AD),
            accentLight = Color(0xFFE885C2),
            accentMuted = Color(0x66D650AD),
            panelScrim = Color(0xCC3650A8),
            osdScrim = Color(0x993650A8),
            nowLine = Color(0xFFFF6678),
            nowFill = Color(0x33D650AD),
            live = Color(0xFFFF6678),
            pipPreviewOutline = Color(0xFFD650AD),
            focusFill = Color(0x33D650AD),
            textPrimary = Color(0xFF3650A8),
            textSecondary = Color(0xFF263A85),
            textTertiary = Color(0x99263A85),
            textDisabled = Color(0x66263A85),
            success = Color(0xFFD650AD),
            warning = Color(0xFFFF6678),
            info = Color(0xFFE885C2),
            divider = Color(0x223650A8),
            outline = Color(0x33D650AD),
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
