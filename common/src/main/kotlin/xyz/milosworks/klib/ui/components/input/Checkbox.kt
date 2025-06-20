package xyz.milosworks.klib.ui.components.input

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.components.theme.LocalTheme
import xyz.milosworks.klib.ui.components.theme.TextureStates
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn
import xyz.milosworks.klib.ui.utils.NinePatchThemeState
import xyz.milosworks.klib.ui.utils.SimpleThemeState
import xyz.milosworks.klib.ui.utils.extensions.ninePatchTexture

@Composable
fun Checkbox(
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    texture: String = "checkbox",
    modifier: Modifier = Modifier
) {
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    var hovered by remember { mutableStateOf(false) }

    Layout(
        measurePolicy = { _, _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
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
                val state = composableTheme.getState(
                    when {
                        checked && hovered -> TextureStates.CLICKED_AND_HOVERED
                        hovered -> TextureStates.HOVERED
                        checked -> TextureStates.CLICKED
                        else -> TextureStates.DEFAULT
                    },
                    theme.mode
                )

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
        modifier = Modifier
            .debug("Hovered: $hovered", "Texture: $texture")
            .onPointerEvent(PointerEventType.ENTER) { _, e -> hovered = true; e.consume() }
            .onPointerEvent(PointerEventType.EXIT) { _, e -> hovered = false; e.consume() }
            .onPointerEvent(PointerEventType.PRESS) { _, e -> onCheckedChange(!checked); e.consume() }
            .run {
                if (!composableTheme.isNinepatch) with(composableTheme.states["default"] as SimpleThemeState) {
                    sizeIn(
                        minWidth = width,
                        minHeight = height
                    )
                } else this
            } then modifier,
    )
}