package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState

@Composable
fun Checkbox(enabled: Boolean = false, texture: String = "checkbox", modifier: Modifier = Modifier) {
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    var active by remember { mutableStateOf(enabled) }
    var hovered by remember { mutableStateOf(false) }

    Layout(
        measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
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
                val state = composableTheme.getState(
                    when {
                        !enabled -> TextureStates.DISABLED
                        hovered && active -> TextureStates.CLICKED_AND_HOVERED
                        hovered -> TextureStates.HOVERED
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
        // TODO: Add a modifier for size if texture is not ninepatch for all components like Slot
        modifier = Modifier
            .onPointerEvent(PointerEventType.ENTER) { _, e -> hovered = true; e.consume() }
            .onPointerEvent(PointerEventType.EXIT) { _, e -> hovered = false; e.consume() }
            .onPointerEvent(PointerEventType.PRESS) { _, e -> active = (!active); e.consume() }
                then modifier,
    )
}