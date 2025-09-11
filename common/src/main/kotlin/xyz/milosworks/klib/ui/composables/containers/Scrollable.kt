package xyz.milosworks.klib.ui.composables.containers

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.*
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.*
import xyz.milosworks.klib.ui.utils.KColor
import kotlin.math.max
import kotlin.math.roundToInt

private const val SCROLLBAR_THICKNESS = 4
private const val SCROLL_SENSITIVITY = 15.0
private const val SCROLLBAR_FADE_DURATION_MS = 1000L
private const val MIN_SCROLLBAR_THUMB_SIZE = 10

/**
 * The direction of scrolling for a [Scrollable] container.
 */
enum class ScrollDirection {
    VERTICAL, HORIZONTAL;

    fun choose(
        horizontal: Double,
        vertical: Double
    ): Double = when (this) {
        VERTICAL -> vertical
        HORIZONTAL -> horizontal
    }
}

/**
 * Manages the state for a [Scrollable] composable.
 *
 * Can be created and remembered using [rememberScrollableState].
 */
@Stable
class ScrollableState {
    var scrollOffset by mutableStateOf(0.0)

    var currentScrollPosition by mutableStateOf(0.0)

    var maxScroll by mutableStateOf(0)
    var childSize by mutableStateOf(0)
    var containerSize by mutableStateOf(0)

    var isDraggingScrollbar by mutableStateOf(false)
    var lastInteractTime by mutableStateOf(0L)

    fun onInteraction() {
        lastInteractTime = System.currentTimeMillis()
    }

    fun scrollBy(delta: Double) {
        scrollOffset =
            (scrollOffset + delta).coerceIn(
                0.0,
                maxScroll.toDouble()
            )
        onInteraction()
    }
}

/**
 * Creates and remembers a [ScrollableState].
 */
@Composable
fun rememberScrollableState(): ScrollableState {
    return remember { ScrollableState() }
}

/**
 * A container that allows its single child to be scrolled if the child's content
 * is larger than the container's bounds.
 *
 * @param direction The direction in which the content can be scrolled.
 * @param scrollbarColor The color of the scrollbar thumb.
 * @param modifier The modifier to be applied to the layout.
 * @param state The state object that holds and controls the scroll position. Defaults to a remembered state.
 * @param content The single child composable to be made scrollable.
 */
@Composable
fun Scrollable(
    direction: ScrollDirection = ScrollDirection.VERTICAL,
    scrollbarColor: KColor = KColor.DARK_GRAY,
    modifier: Modifier = Modifier,
    state: ScrollableState = rememberScrollableState(),
    content: @Composable () -> Unit
) {
    val measurePolicy = remember(direction) {
        object : MeasurePolicy {
            override fun measure(
                scope: MeasureScope,
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                if (measurables.isEmpty()) return MeasureResult(
                    constraints.minWidth,
                    constraints.minHeight
                ) {}

                val contentConstraints =
                    if (direction == ScrollDirection.VERTICAL) {
                        constraints.copy(
                            minHeight = 0,
                            maxHeight = Int.MAX_VALUE
                        )
                    } else {
                        constraints.copy(
                            minWidth = 0,
                            maxWidth = Int.MAX_VALUE
                        )
                    }

                val placeable = measurables.first()
                    .measure(contentConstraints)

                state.childSize = direction.choose(
                    placeable.width.toDouble(),
                    placeable.height.toDouble()
                ).toInt()
                state.containerSize =
                    direction.choose(
                        constraints.maxWidth.toDouble(),
                        constraints.maxHeight.toDouble()
                    ).toInt()
                state.maxScroll = max(
                    0,
                    state.childSize - state.containerSize
                )
                state.scrollOffset =
                    state.scrollOffset.coerceIn(
                        0.0,
                        state.maxScroll.toDouble()
                    )

                val finalWidth = constraints.maxWidth
                val finalHeight = constraints.maxHeight

                return MeasureResult(
                    finalWidth,
                    finalHeight
                ) {
                    val scrollPos =
                        state.currentScrollPosition.roundToInt()
                    if (direction == ScrollDirection.VERTICAL) {
                        placeable.placeAt(0, -scrollPos)
                    } else {
                        placeable.placeAt(-scrollPos, 0)
                    }
                }
            }
        }
    }

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
                guiGraphics.enableScissor(
                    x,
                    y,
                    x + node.width,
                    y + node.height
                )
            }

            override fun renderAfterChildren(
                node: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                state.currentScrollPosition += (state.scrollOffset - state.currentScrollPosition) * 0.4 * partialTick

                if (state.maxScroll > 0) {
                    val timeSinceInteract =
                        System.currentTimeMillis() - state.lastInteractTime
                    if (!(timeSinceInteract > SCROLLBAR_FADE_DURATION_MS && !state.isDraggingScrollbar)) {
                        val fadeAlpha =
                            if (state.isDraggingScrollbar) 1f else 1f - (timeSinceInteract.toFloat() / SCROLLBAR_FADE_DURATION_MS)
                        val alpha = Mth.clamp(
                            (fadeAlpha * scrollbarColor.alpha).toInt(),
                            0,
                            255
                        )
                        if (alpha > 0) {
                            val colorWithAlpha =
                                (scrollbarColor.rgb) or (alpha shl 24)
                            val trackSize =
                                state.containerSize
                            val thumbSize = max(
                                MIN_SCROLLBAR_THUMB_SIZE,
                                (trackSize.toFloat() / state.childSize * trackSize).toInt()
                            )
                            val scrollPercentage =
                                if (state.maxScroll > 0) state.currentScrollPosition / state.maxScroll else 0.0
                            val thumbPosition =
                                scrollPercentage * (trackSize - thumbSize)

                            if (direction == ScrollDirection.VERTICAL) {
                                val thumbX =
                                    x + node.width - SCROLLBAR_THICKNESS
                                val thumbY =
                                    y + thumbPosition.roundToInt()
                                guiGraphics.fill(
                                    thumbX,
                                    thumbY,
                                    thumbX + SCROLLBAR_THICKNESS,
                                    thumbY + thumbSize,
                                    colorWithAlpha
                                )
                            } else {
                                val thumbX =
                                    x + thumbPosition.roundToInt()
                                val thumbY =
                                    y + node.height - SCROLLBAR_THICKNESS
                                guiGraphics.fill(
                                    thumbX,
                                    thumbY,
                                    thumbX + thumbSize,
                                    thumbY + SCROLLBAR_THICKNESS,
                                    colorWithAlpha
                                )
                            }
                        }
                    }
                }

                guiGraphics.disableScissor()
            }
        },
        modifier = modifier
            .onScroll<UINode> { _, event ->
                state.scrollBy(-event.scrollY * SCROLL_SENSITIVITY)
                event.consume()
            }
            .onPointerEvent<UINode>(PointerEventType.PRESS) { node, event ->
                val (mouseX, mouseY) = event.mouseX to event.mouseY
                val (nodeX, nodeY) = node.x to node.y

                val scrollbarBounds =
                    if (direction == ScrollDirection.VERTICAL) {
                        Vector4f(
                            (nodeX + node.width - SCROLLBAR_THICKNESS).toFloat(),
                            nodeY.toFloat(),
                            (nodeX + node.width).toFloat(),
                            (nodeY + node.height).toFloat()
                        )
                    } else {
                        Vector4f(
                            nodeX.toFloat(),
                            (nodeY + node.height - SCROLLBAR_THICKNESS).toFloat(),
                            (nodeX + node.width).toFloat(),
                            (nodeY + node.height).toFloat()
                        )
                    }

                if (mouseX >= scrollbarBounds.x && mouseX <= scrollbarBounds.z && mouseY >= scrollbarBounds.y && mouseY <= scrollbarBounds.w) {
                    state.isDraggingScrollbar = true
                    state.onInteraction()
                    event.consume()
                }
            }
            .onPointerEvent<UINode>(PointerEventType.GLOBAL_RELEASE) { _, _ ->
                state.isDraggingScrollbar = false
            }
            .onDrag<UINode> { _, event ->
                if (!state.isDraggingScrollbar) return@onDrag

                val pixelDelta = direction.choose(
                    event.dragX,
                    event.dragY
                )
                val trackSize = state.containerSize
                val thumbSize =
                    max(
                        MIN_SCROLLBAR_THUMB_SIZE,
                        (trackSize.toFloat() / state.childSize * trackSize).toInt()
                    )

                if (trackSize > thumbSize) {
                    val scrollDelta =
                        pixelDelta * (state.maxScroll.toFloat() / (trackSize - thumbSize))
                    state.scrollBy(scrollDelta)
                }
                event.consume()
            }
            .onKeyEvent { _, event ->
                val amount =
                    if (direction == ScrollDirection.VERTICAL) state.containerSize * 0.8 else state.containerSize * 0.8
                when (event.keyCode) {
                    GLFW.GLFW_KEY_DOWN -> if (direction == ScrollDirection.VERTICAL) state.scrollBy(
                        SCROLL_SENSITIVITY
                    )

                    GLFW.GLFW_KEY_UP -> if (direction == ScrollDirection.VERTICAL) state.scrollBy(
                        -SCROLL_SENSITIVITY
                    )

                    GLFW.GLFW_KEY_RIGHT -> if (direction == ScrollDirection.HORIZONTAL) state.scrollBy(
                        SCROLL_SENSITIVITY
                    )

                    GLFW.GLFW_KEY_LEFT -> if (direction == ScrollDirection.HORIZONTAL) state.scrollBy(
                        -SCROLL_SENSITIVITY
                    )

                    GLFW.GLFW_KEY_PAGE_DOWN -> state.scrollBy(
                        amount
                    )

                    GLFW.GLFW_KEY_PAGE_UP -> state.scrollBy(
                        -amount
                    )

                    else -> return@onKeyEvent
                }
                event.consume()
            },
        content = content
    )
}