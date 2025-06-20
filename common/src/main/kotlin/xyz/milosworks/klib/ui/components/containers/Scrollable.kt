package xyz.milosworks.klib.ui.components.containers

import androidx.compose.runtime.*
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.layout.measure.Measurable
import xyz.milosworks.klib.ui.layout.measure.MeasurePolicy
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.MeasureScope
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import kotlin.math.max
import kotlin.math.roundToInt

enum class ScrollDirection {
    VERTICAL,
    HORIZONTAL;

    fun choose(horizontal: Double, vertical: Double): Double = when (this) {
        VERTICAL -> vertical
        HORIZONTAL -> horizontal
    }

    val lessKeycode: Int
        get() = when (this) {
            VERTICAL -> GLFW.GLFW_KEY_UP
            HORIZONTAL -> GLFW.GLFW_KEY_LEFT
        }

    val moreKeycode: Int
        get() = when (this) {
            VERTICAL -> GLFW.GLFW_KEY_DOWN
            HORIZONTAL -> GLFW.GLFW_KEY_RIGHT
        }
}

@Composable
fun Scrollable(
    direction: ScrollDirection = ScrollDirection.VERTICAL,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scrollOffset by remember { mutableStateOf(0.0) }
    var currentScrollPosition by remember { mutableStateOf(0.0) }
    var maxScroll by remember { mutableStateOf(0) }
    var childSize by remember { mutableStateOf(0) }
    var scrollbarOffset by remember { mutableStateOf(0) }
    var lastInteractTime by remember { mutableStateOf(0L) }
    var scrollbaring by remember { mutableStateOf(false) }
    var scrollbarLength by remember { mutableStateOf(0.0) }

    val measurePolicy = remember(Alignment.TopStart) {
        object : MeasurePolicy {
            override fun measure(
                scope: MeasureScope,
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                if (measurables.isEmpty()) return MeasureResult(0, 0) {}

                // First, measure the content with the container's constraints
                val contentConstraints = if (direction == ScrollDirection.VERTICAL) {
                    constraints.copy(minHeight = 0, maxHeight = Int.MAX_VALUE)
                } else {
                    constraints.copy(minWidth = 0, maxWidth = Int.MAX_VALUE)
                }

                val placeable = measurables[0].measure(contentConstraints)

                // Compute maxScroll based on content size and container size
                childSize = if (direction == ScrollDirection.VERTICAL) placeable.height else placeable.width
                val containerSize =
                    if (direction == ScrollDirection.VERTICAL) constraints.minHeight else constraints.minWidth
                maxScroll = max(0, childSize - containerSize)

                // Clamp scroll position
                scrollOffset = scrollOffset.coerceIn(0.0, maxScroll.toDouble())

                // Compute sizes for the MeasureResult
                val width = constraints.minWidth
                val height = constraints.minHeight

                return MeasureResult(width, height) {
                    // Position the content with the current scroll offset
                    val scrollPos = currentScrollPosition.roundToInt()
                    if (direction == ScrollDirection.VERTICAL) {
                        placeable.placeAt(0, -scrollPos)
                    } else {
                        placeable.placeAt(-scrollPos, 0)
                    }
                }
            }
        }
    }
}
