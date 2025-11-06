package xyz.milosworks.klib.ui.composables.input

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.composables.theme.LocalTheme
import xyz.milosworks.klib.ui.composables.theme.TextureStates
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.containers.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.sizeIn
import xyz.milosworks.klib.ui.utils.SimpleThemeState
import xyz.milosworks.klib.ui.utils.extensions.drawThemeState

//@Composable
//fun Checkbox(
//    checked: Boolean = false,
//    onCheckedChange: (Boolean) -> Unit,
//    texture: String = "checkbox",
//    modifier: Modifier = Modifier
//) {
//    val theme = LocalTheme.current
//    val composableTheme = theme.getComposableTheme(texture)
//
//    var hovered by remember { mutableStateOf(false) }
//
//    Layout(
//        measurePolicy = { _, _, constraints ->
//            println("minwidth: ${constraints.minWidth}, minheight: ${constraints.minHeight}")
//            MeasureResult(constraints.minWidth, constraints.minHeight) {}
//        },
//        renderer = object : DefaultRenderer() {
//            override fun render(
//                node: UINode,
//                x: Int,
//                y: Int,
//                guiGraphics: GuiGraphics,
//                mouseX: Int,
//                mouseY: Int,
//                partialTick: Float
//            ) {
//                val state = composableTheme.getState(
//                    when {
//                        checked && hovered -> TextureStates.CLICKED_AND_HOVERED
//                        hovered -> TextureStates.HOVERED
//                        checked -> TextureStates.CLICKED
//                        else -> TextureStates.DEFAULT
//                    },
//                    theme.mode
//                )
//
//                if (composableTheme.isNinepatch) return guiGraphics.ninePatchTexture(
//                    x,
//                    y,
//                    node.width,
//                    node.height,
//                    state as NinePatchThemeState
//                )
//
//                guiGraphics.blit(
//                    (state as SimpleThemeState).texture,
//                    x,
//                    y,
//                    state.width,
//                    state.height,
//                    state.u.toFloat(),
//                    state.v.toFloat(),
//                    state.uWidth,
//                    state.vHeight,
//                    state.textureSize.width,
//                    state.textureSize.height,
//                )
//
//                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
//            }
//        },
//        modifier = Modifier
//            .debug("Hovered: $hovered", "Texture: $texture")
//            .onPointerEvent<UINode>(PointerEventType.ENTER) { _, e -> hovered = true; e.consume() }
//            .onPointerEvent<UINode>(PointerEventType.EXIT) { _, e -> hovered = false; e.consume() }
//            .onPointerEvent<UINode>(PointerEventType.PRESS) { _, e -> onCheckedChange(!checked); e.consume() }
//            .run {
//                if (!composableTheme.isNinepatch) with(composableTheme.states["default"] as SimpleThemeState) {
//                    println("width: $width, height: $height")
//                    sizeIn(
//                        minWidth = width,
//                        minHeight = height
//                    )
//                } else this
//            } then modifier,
//    )
//}

@Composable
fun Checkbox(
    checked: Boolean = false,
    modifier: Modifier = Modifier,
    texture: String = "checkbox",
    onCheckedChange: (Boolean) -> Unit,
) {
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    CheckboxCore(
        checked,
        (if (!composableTheme.isNinepatch) with(composableTheme.states["default"] as SimpleThemeState) {
            Modifier.sizeIn(
                minWidth = width,
                minHeight = height
            )
        } else Modifier).then(modifier),
        onCheckedChange
    ) { isHovered ->
        Layout(
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
                            checked && isHovered -> TextureStates.CLICKED_AND_HOVERED
                            isHovered -> TextureStates.HOVERED
                            checked -> TextureStates.CLICKED
                            else -> TextureStates.DEFAULT
                        },
                        theme.mode
                    )

                    guiGraphics.drawThemeState(state, x, y, node.width, node.height)

                    super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
                }
            },
            modifier = Modifier
        )
    }
}

@Composable
fun CheckboxCore(
    checked: Boolean = false,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable (isHovered: Boolean) -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .debug("Hovered: $hovered")
            .onPointerEvent<UINode>(PointerEventType.ENTER) { _, e -> hovered = true; e.consume() }
            .onPointerEvent<UINode>(PointerEventType.EXIT) { _, e -> hovered = false; e.consume() }
            .onPointerEvent<UINode>(PointerEventType.PRESS) { _, e -> onCheckedChange(!checked); e.consume() }
                then modifier
    ) {
        content(hovered)
    }
}