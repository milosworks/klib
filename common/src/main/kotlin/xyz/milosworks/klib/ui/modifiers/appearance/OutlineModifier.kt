package xyz.milosworks.klib.ui.modifiers.appearance

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.utils.KColor

/**
 * A modifier that adds an outline to a composable with a specified color.
 *
 * This modifier allows you to add an outline around the composable.
 *
 * @param color The color of the outline (ARGB format).
 */
data class OutlineModifier(val color: Int) : Modifier.Element<OutlineModifier> {
    override fun mergeWith(other: OutlineModifier): OutlineModifier =
        throw UnsupportedOperationException("not implemented")

    override fun toString(): String = "OutlineModifier(color=#${
        String.format(
            "%08X",
            color
        )
    })"
}

/**
 * Adds an outline to the composable with a specified color.
 *
 * This modifier adds an outline around the composable. The color of the outline is provided as a [KColor].
 *
 * @param color The color of the outline, specified as a [KColor].
 */
@Stable
fun Modifier.outline(color: KColor) = this then OutlineModifier(color.argb)

/**
 * Adds an outline to the composable with a specified color.
 *
 * This modifier adds an outline around the composable. The color of the outline is provided as an [Int]
 * (in ARGB format).
 *
 * @param color The color of the outline, specified as an [Int] (ARGB format).
 */
@Stable
fun Modifier.outline(color: Int): Modifier = this then OutlineModifier(color)