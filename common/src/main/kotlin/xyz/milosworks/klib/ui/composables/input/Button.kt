package xyz.milosworks.klib.ui.composables.input

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.composables.theme.LocalTheme
import xyz.milosworks.klib.ui.composables.theme.TextureStates
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.containers.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn
import xyz.milosworks.klib.ui.utils.extensions.drawThemeState

@Composable
fun Button(
    onClick: (UINode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    texture: String = "button",
    content: @Composable () -> Unit = {}
) {
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    ButtonCore(
        onClick,
        modifier,
        enabled
    ) { isHovered, isPressed ->
        Layout(
            content = content,
            measurePolicy = BoxMeasurePolicy(Alignment.Center),
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
                            isPressed && composableTheme.hasState(
                                TextureStates.CLICKED,
                                theme.mode
                            ) -> TextureStates.CLICKED

                            isHovered -> TextureStates.HOVERED
                            else -> TextureStates.DEFAULT
                        },
                        theme.mode
                    )

                    guiGraphics.drawThemeState(state, x, y, node.width, node.height)

                    return super.render(
                        node,
                        x,
                        y,
                        guiGraphics,
                        mouseX,
                        mouseY,
                        partialTick
                    )
                }
            },
            modifier = modifier.apply {
                if (!composableTheme.isNinepatch) with(composableTheme.states["default"]!!) {
                    sizeIn(
                        minWidth = textureSize.width,
                        minHeight = textureSize.height
                    )
                }
            }
        )
    }
}

@Composable
fun ButtonCore(
    onClick: (UINode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (isHovered: Boolean, isPressed: Boolean) -> Unit
) {
    var hovered by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .debug(
                "Hovered: $hovered",
                "Clicked: $pressed",
                "Enabled: $enabled",
            )
            .onPointerEvent<UINode>(PointerEventType.ENTER) { _, e ->
                if (!enabled) return@onPointerEvent

                hovered = true

                GLFW.glfwSetCursor(
                    Minecraft.getInstance().window.window,
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
                )

                e.consume()
            }
            .onPointerEvent<UINode>(PointerEventType.EXIT) { _, e ->
                if (!enabled) return@onPointerEvent

                hovered = false

                GLFW.glfwSetCursor(
                    Minecraft.getInstance().window.window,
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
                )

                e.consume()
            }
            .onPointerEvent<UINode>(PointerEventType.PRESS) { node, e ->
                if (enabled) {
                    pressed = true
                    onClick(node)
                    e.consume()
                }
            }
            .onPointerEvent<LayoutNode>(PointerEventType.GLOBAL_RELEASE) { node, e ->
                if (enabled) pressed = false

//                if (!node.isBounded(
//                        e.mouseX.toInt(),
//                        e.mouseY.toInt()
//                    )
//                ) {
//                    hovered = false
//                    GLFW.glfwSetCursor(
//                        Minecraft.getInstance().window.window,
//                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
//                    )
//                }
            }
                then modifier
    ) { content(hovered, pressed) }
}