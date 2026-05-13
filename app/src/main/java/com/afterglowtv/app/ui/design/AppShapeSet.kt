package com.afterglowtv.app.ui.design

/**
 * The shape half of a AfterglowTV theme. Each enum picks one of the shape
 * variants from `docs/mockups/primitives.html`. Themes bundle a palette
 * (colors) and a shape set (silhouettes / forms) — both swappable
 * independently, both reactive via [AppShapes].
 *
 * Adding a new variant: add the enum constant here, render it inside the
 * corresponding wrapper composable (e.g. AfterglowTVButton.kt for buttons),
 * and add it to the shape picker.
 */
data class AppShapeSet(
    val id: String,
    val displayName: String,
    val description: String,
    val button: ButtonStyle,
    val epgCell: EpgCellStyle,
    val epgLiveCell: EpgLiveCellStyle,
    val textField: TextFieldStyle,
    val channelRow: ChannelRowStyle,
    val pill: PillStyle,
    val focus: FocusStyle,
    val progress: ProgressStyle,
) {
    enum class ButtonStyle { PILL, SHARP, SOFT, CUT_CORNER, STRIPE, GHOST, GLASS, DOUBLE_SHADOW }
    enum class EpgCellStyle { RECTANGULAR, SOFT, ACCENT_STRIPE, BEVELED, DOUBLE_EDGE }
    enum class EpgLiveCellStyle { NOW_FILL, GLOW, CORNER_TAG, DOUBLE_EDGE }
    enum class TextFieldStyle { RECT, SOFT, PILL, ACCENT_STRIPE, UNDERLINE, DOUBLE_LINE, BRACKETED, PREFIXED }
    enum class ChannelRowStyle { FLAT, SOFT, STRIPE, STRIPE_FILL, BEVELED, WITH_PROGRESS, BRACKETED }
    enum class PillStyle { ROUND, SQUARE, SOFT, STRIPED, BEVEL_CUT, SOLID, OUTLINE, LIVE_BLINK }
    enum class FocusStyle { BORDER, FILL, STRIPE, STRIPE_FILL, GLOW, CUT_CORNER, DOUBLE_RING, CORNER_BRACKETS }
    enum class ProgressStyle { FLAT, PILL, HAIRLINE, SEGMENTED, DOTS }

    companion object {
        /** Author's recommended bundle — every "PICK" from primitives.html. */
        val AuroraDefault = AppShapeSet(
            id = "aurora_default",
            displayName = "Aurora Default",
            description = "Cut-corner buttons, beveled EPG cells, stripe everywhere. The signature AfterglowTV silhouette.",
            button = ButtonStyle.CUT_CORNER,
            epgCell = EpgCellStyle.BEVELED,
            epgLiveCell = EpgLiveCellStyle.NOW_FILL,
            textField = TextFieldStyle.ACCENT_STRIPE,
            channelRow = ChannelRowStyle.WITH_PROGRESS,
            pill = PillStyle.STRIPED,
            focus = FocusStyle.STRIPE_FILL,
            progress = ProgressStyle.HAIRLINE,
        )

        /** Default shape set. Heavier glass + glow focus haloes + bracketed rows. */
        val Halo = AppShapeSet(
            id = "halo",
            displayName = "Halo",
            description = "Glass buttons over backdrop blur, prefixed fields, bracketed rows, glow focus haloes.",
            button = ButtonStyle.GLASS,
            epgCell = EpgCellStyle.ACCENT_STRIPE,
            epgLiveCell = EpgLiveCellStyle.GLOW,
            textField = TextFieldStyle.PREFIXED,
            channelRow = ChannelRowStyle.BRACKETED,
            pill = PillStyle.STRIPED,
            focus = FocusStyle.GLOW,
            progress = ProgressStyle.HAIRLINE,
        )

        val ALL: List<AppShapeSet> = listOf(Halo, AuroraDefault)

        fun byId(id: String?): AppShapeSet = ALL.firstOrNull { it.id == id } ?: Halo
    }
}
