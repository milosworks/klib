package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState
import xyz.milosworks.klib.ui.util.SimpleThemeState

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
        measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
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