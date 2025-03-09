package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Alignment
import xyz.milosworks.klib.ui.layout.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState

@Composable
fun Button(
    text: Component? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    texture: String = "button",
    onClick: (UINode) -> Unit
) {
    val measurePolicy = remember(Alignment.Center) { BoxMeasurePolicy(Alignment.Center) }
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    var hovered: Boolean by remember { mutableStateOf(false) }
    var clicked: Boolean by remember { mutableStateOf(false) }

    Layout(
        measurePolicy = measurePolicy,
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
        modifier = Modifier
            .onPointerEvent(PointerEventType.ENTER) { _, e ->
                println("enter button")
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
            }
                then modifier,
    ) {
        if (text != null) Text(text)
    }
}