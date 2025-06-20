package xyz.milosworks.klib.ui.components.containers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.components.theme.LocalTheme
import xyz.milosworks.klib.ui.components.theme.TextureStates
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.containers.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn
import xyz.milosworks.klib.ui.utils.NinePatchThemeState
import xyz.milosworks.klib.ui.utils.SimpleThemeState
import xyz.milosworks.klib.ui.utils.extensions.ninePatchTexture

@Composable
fun Surface(
    contentAlignment: Alignment = Alignment.Companion.TopStart,
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