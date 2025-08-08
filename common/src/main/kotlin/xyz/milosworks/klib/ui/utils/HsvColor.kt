package xyz.milosworks.klib.ui.utils

import androidx.compose.runtime.Immutable

/**
 * A color representation based on Hue, Saturation, Value, and Alpha.
 * This is ideal for color pickers.
 *
 * @param hue The hue of the color, ranging from 0.0 to 1.0.
 * @param saturation The saturation of the color, ranging from 0.0 to 1.0.
 * @param value The value (brightness) of the color, ranging from 0.0 to 1.0.
 * @param alpha The alpha (transparency) of the color, ranging from 0.0 to 1.0.
 */
@Immutable
data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float,
) {
    /**
     * Converts this HSV color to a KColor (ARGB).
     */
    fun toKColor(): KColor {
        return KColor.ofHsv(hue, saturation, value, alpha)
    }

    companion object {
        /**
         * Creates an HsvColor from a KColor instance.
         */
        fun from(color: KColor): HsvColor {
            val hsb = java.awt.Color.RGBtoHSB(color.red, color.green, color.blue, null)
            return HsvColor(
                hue = hsb[0],
                saturation = hsb[1],
                value = hsb[2],
                alpha = color.alpha / 255f
            )
        }
    }
}