package xyz.milosworks.klib.ui.composables.basic

import androidx.compose.runtime.Composable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.layout.primitive.Size
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn

/**
 * Returns the size (width, height) of the text depending on font and scale
 */
fun getTextSize(
    text: Component,
    scale: Float = 1f,
    font: Font = Minecraft.getInstance().font
): Size =
    Size((font.width(text) * scale).toInt(), (font.lineHeight * scale).toInt())


/**
 * Renders text with support for scaling, custom fonts, and color.
 *
 * This composable displays text using Minecraft's font renderer, with optional
 * scaling, font choice, and color adjustments.
 *
 * Example usage:
 * ```
 * Text("Hello, Minecraft!", fontScale = 1.5f, color = 0xFF00FF)
 * ```
 *
 * @param text The text to be rendered.
 * @param fontScale The scaling factor for the font. The default is 1.0, which means no scaling.
 * @param font The font to use for rendering the text. The default is Minecraft's standard font.
 * @param color The color of the text, represented as an ARGB integer. Default is white (0xFFFFFFFF).
 */
@Composable
fun Text(
    text: Component,
    fontScale: Float = 1f,
    font: Font = Minecraft.getInstance().font,
    color: Int = 0xFFFFFFFF.toInt(),
    modifier: Modifier = Modifier
) {
    Layout(
        measurePolicy = { _, _, constraints ->
            MeasureResult(
                constraints.minWidth,
                constraints.minHeight
            ) {}
        },
        renderer = object : Renderer {
            override fun render(
                node: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                if (fontScale != 1f) {
                    guiGraphics.pose().apply {
                        pushPose()
                        scale(fontScale, fontScale, fontScale)
                        // Adjust for scaling
                        translate(x / fontScale, y / fontScale, 0f)
                    }
                    // Draw at (0,0) relative to new position
                    guiGraphics.drawString(font, text, 0, 0, color)
                    guiGraphics.pose().popPose()
                } else {
                    // Use original position
                    guiGraphics.drawString(font, text, x, y, color)
                }

                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
        },
        modifier = getTextSize(text, fontScale, font).run {
            Modifier.sizeIn(
                minWidth = width,
                minHeight = height
            )
        } then modifier,
    )
}