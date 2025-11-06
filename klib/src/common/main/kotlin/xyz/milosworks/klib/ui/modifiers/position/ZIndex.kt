package xyz.milosworks.klib.ui.modifiers.position

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.modifiers.core.Modifier

/**
 * A modifier that specifies the z-index of a composable relative to its siblings.
 *
 * This modifier allows you to control the rendering and input order of composables. Elements with a
 * higher z-index will be rendered on top of their siblings with a lower z-index. The final
 * depth is calculated hierarchically.
 *
 * @param zIndex The z-index value. Higher values are rendered on top of siblings.
 */
data class ZIndexModifier(val zIndex: Float) : Modifier.Element<ZIndexModifier> {
    /**
     * When multiple zIndex modifiers are chained, the last one applied (the 'other') wins.
     */
    override fun mergeWith(other: ZIndexModifier): ZIndexModifier = other

    override fun toString(): String = "ZIndexModifier(zIndex=$zIndex)"
}

/**
 * Applies a z-index to the composable, affecting drawing and input order relative to its siblings.
 *
 * This modifier controls the order in which composables are rendered. An element with a higher
 * z-index will appear in front of a sibling element with a lower z-index. This effect is
 * hierarchical: the z-index is added to the parent's calculated depth.
 *
 * @param zIndex The z-index value to apply.
 */
@Stable
fun Modifier.zIndex(zIndex: Float): Modifier = this then ZIndexModifier(zIndex)