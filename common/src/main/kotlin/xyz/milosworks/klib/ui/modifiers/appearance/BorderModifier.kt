package xyz.milosworks.klib.ui.modifiers.appearance

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.modifiers.core.ContentDrawScope
import xyz.milosworks.klib.ui.modifiers.core.DrawModifier
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.utils.KColor
import xyz.milosworks.klib.ui.utils.extensions.drawRectOutline

/**
 * A modifier that adds a border around a composable.
 *
 * @param thickness The width of the border in pixels.
 * @param color The ARGB color of the border.
 */
data class BorderModifier(val color: Int, val thickness: Int) : Modifier.Element<BorderModifier>,
    DrawModifier {
    override fun mergeWith(other: BorderModifier): BorderModifier = other

    override fun ContentDrawScope.draw() {
        guiGraphics.drawRectOutline(x, y, width, height, color, thickness)
        drawContent()
    }

    override fun toString(): String =
        "BorderModifier(width=${thickness}, color=#${String.format("%08X", color)})"
}

/**
 * Adds a border to the composable with a specified width and color.
 *
 * @param thickness The width of the border in pixels.
 * @param color The color of the border.
 */
@Stable
fun Modifier.border(color: KColor, thickness: Int = 1): Modifier =
    this then BorderModifier(color.argb, thickness)

/**
 * Adds a border to the composable with a specified width and color.
 *
 * @param thickness The width of the border in pixels.
 * @param color The color of the border.
 */
@Stable
fun Modifier.border(color: Int, thickness: Int = 1): Modifier =
    this then BorderModifier(color, thickness)
