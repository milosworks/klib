package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.nodes.UINode

/**
 * Renders a texture from a specified location with the given UV offsets and texture size.
 *
 * Example usage:
 * ```
 * Texture(loc = myTextureLocation, uOffset = 0f, vOffset = 0f, u = 16, v = 16, textureWidth = 64, textureHeight = 64)
 * ```
 *
 * @param loc The resource location of the texture to be rendered.
 * @param uOffset The horizontal offset (U coordinate) to start the texture from.
 * @param vOffset The vertical offset (V coordinate) to start the texture from.
 * @param u The horizontal size (U dimension) of the texture to be displayed.
 * @param v The vertical size (V dimension) of the texture to be displayed.
 * @param textureWidth The width of the texture to be used.
 * @param textureHeight The height of the texture to be used.
 */
@Composable
fun Texture(
    loc: ResourceLocation,
    uOffset: Float,
    vOffset: Float,
    u: Int,
    v: Int,
    textureWidth: Int,
    textureHeight: Int,
    modifier: Modifier = Modifier
) {
    Layout(
        measurePolicy = { _, constraints ->
            MeasureResult(constraints.minWidth, constraints.minHeight) {}
        },
        renderer = object : DefaultRenderer() {
            override fun render(
                node: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                guiGraphics.blit(
                    loc,
                    x,
                    y,
                    node.width,
                    node.height,
                    uOffset,
                    vOffset,
                    u,
                    v,
                    textureWidth,
                    textureHeight
                )

                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
        },
        modifier = Modifier.debug(loc.toString()) then modifier,
    )
}