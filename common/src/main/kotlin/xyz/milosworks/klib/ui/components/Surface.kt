package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Alignment
import xyz.milosworks.klib.ui.layout.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState

@Composable
fun Surface(
    contentAlignment: Alignment = Alignment.TopStart,
    modifier: Modifier = Modifier,
    texture: String = "surface",
    content: @Composable () -> Unit
) {
    val measurePolicy = remember(contentAlignment) { BoxMeasurePolicy(contentAlignment) }
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)
    val state = composableTheme.getState(TextureStates.DEFAULT, theme.mode)

    Layout(
        measurePolicy,
        object : Renderer {
            override fun render(
                node: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                if (composableTheme.isNinepatch) return guiGraphics.ninePatchTexture(
                    x,
                    y,
                    node.width,
                    node.height,
                    state as NinePatchThemeState
                )

                guiGraphics.blit(
                    state.texture,
                    x,
                    y,
                    state.textureSize.width,
                    state.textureSize.height,
                    state.u.toFloat(),
                    state.v.toFloat(),
                    state.textureSize.width,
                    state.textureSize.height,
                    state.textureSize.width,
                    state.textureSize.height
                )
            }
        },
        Modifier.debug(state.texture.toString()) then modifier,
        content
    )
}