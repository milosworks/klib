package xyz.milosworks.klib.ui.modifiers.core

import net.minecraft.client.gui.GuiGraphics

/**
 * A modifier element that allows for custom drawing before or after the content of a composable.
 *
 * This is used to implement visual effects like backgrounds, borders or alpha changes.
 */
interface DrawModifier {
    /**
     * The drawing logic for this modifier.
     *
     * This function is called during the render phase. It provides a `ContentDrawScope`
     * which allows you to call `drawContent()` to render the wrapped content. This enables
     * drawing underneath the content (e.g., for a background) or over it.
     */
    fun ContentDrawScope.draw()
}

/**
 * The scope within which a `DrawModifier` can draw.
 */
interface ContentDrawScope {
    val guiGraphics: GuiGraphics
    val width: Int
    val height: Int
    val x: Int
    val y: Int

    /**
     * Draws the content of the composable that this modifier is wrapping.
     */
    fun drawContent()
}