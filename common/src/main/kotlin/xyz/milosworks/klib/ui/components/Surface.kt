package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Alignment
import xyz.milosworks.klib.ui.layout.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState
import xyz.milosworks.klib.ui.util.SimpleThemeState

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
        object : DefaultRenderer() {
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
                    (state as SimpleThemeState).texture,
                    x,
                    y,
                    state.width,
                    state.height,
                    state.u.toFloat(),
                    state.v.toFloat(),
                    state.uWidth,
                    state.vHeight,
                    state.textureSize.width,
                    state.textureSize.height,
                )

                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
        },
        Modifier.debug(state.texture.toString()).apply {
            if (!composableTheme.isNinepatch) with(composableTheme.states["default"]!!) {
                sizeIn(
                    minWidth = textureSize.width,
                    minHeight = textureSize.height
                )
            }
        } then modifier,
        content
    )
}