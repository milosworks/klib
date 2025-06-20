package xyz.milosworks.klib.ui.components.input

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.components.basic.Text
import xyz.milosworks.klib.ui.components.theme.LocalTheme
import xyz.milosworks.klib.ui.components.theme.TextureStates
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.containers.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn
import xyz.milosworks.klib.ui.utils.NinePatchThemeState
import xyz.milosworks.klib.ui.utils.SimpleThemeState
import xyz.milosworks.klib.ui.utils.extensions.ninePatchTexture

@Composable
fun Button(
    text: Component? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    texture: String = "button",
    onClick: (UINode) -> Unit
) {
    val measurePolicy = remember(Alignment.Companion.Center) { BoxMeasurePolicy(Alignment.Companion.Center) }
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    var hovered: Boolean by remember { mutableStateOf(false) }
    var clicked: Boolean by remember { mutableStateOf(false) }

    Layout(
        measurePolicy = measurePolicy,
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
                        !enabled -> TextureStates.DISABLED
                        clicked && composableTheme.hasState(
                            TextureStates.CLICKED,
                            theme.mode
                        ) -> TextureStates.CLICKED

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
            .debug("Hovered: $hovered", "Clicked: $clicked", "Enabled: $enabled", "Texture: $texture")
            .onPointerEvent(PointerEventType.ENTER) { _, e ->
                hovered = true

                GLFW.glfwSetCursor(
                    Minecraft.getInstance().window.window,
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
                )

                e.consume()
            }
            .onPointerEvent(PointerEventType.EXIT) { _, e ->
                hovered = false

                GLFW.glfwSetCursor(
                    Minecraft.getInstance().window.window,
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
                )

                e.consume()
            }
            .onPointerEvent(PointerEventType.PRESS) { node, e ->
                if (enabled) {
                    clicked = true
                    onClick(node)
                    e.consume()
                }
            }
            .onPointerEvent(PointerEventType.GLOBAL_RELEASE) { node, e ->
                if (enabled) {
                    clicked = false
                }
            }.apply {
                if (!composableTheme.isNinepatch) with(composableTheme.states["default"]!!) {
                    sizeIn(
                        minWidth = textureSize.width,
                        minHeight = textureSize.height
                    )
                }
            } then modifier,
    ) {
        if (text != null) Text(text)
    }
}