package xyz.milosworks.klib.ui.modifiers.input

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.modifiers.core.Modifier

const val LONG_CLICK_THRESHOLD = 500
const val DOUBLE_CLICK_THRESHOLD = 300

enum class PointerEventType {
    /** This specific event will not cancel others if you return true */
    GLOBAL_PRESS,
    PRESS,

    /** This specific event will not cancel others if you return true */
    GLOBAL_RELEASE,
    RELEASE,
    MOVE,
    ENTER,
    EXIT,
    SCROLL,
    DRAG
}

data class OnPointerEventModifier(
    val eventType: PointerEventType, val onEvent: (UINode, PointerEvent) -> Unit
) : Modifier.Element<OnPointerEventModifier> {
    override fun mergeWith(other: OnPointerEventModifier): OnPointerEventModifier = other

    override fun toString(): String = "OnPointerEventModifier(eventType=${eventType.name})"
}

@Stable
fun Modifier.onPointerEvent(type: PointerEventType, onEvent: (UINode, PointerEvent) -> Unit): Modifier =
    this then OnPointerEventModifier(type, onEvent)

@Suppress("UNCHECKED_CAST")
@Stable
fun Modifier.onScroll(onScrollEvent: (UINode, ScrollEvent) -> Unit): Modifier =
    this then OnPointerEventModifier(PointerEventType.SCROLL, onScrollEvent as (UINode, PointerEvent) -> Unit)

@Suppress("UNCHECKED_CAST")
@Stable
fun Modifier.onDrag(onDragEvent: (UINode, DragEvent) -> Unit): Modifier =
    this then OnPointerEventModifier(PointerEventType.DRAG, onDragEvent as (UINode, PointerEvent) -> Unit)

@Stable
fun Modifier.combinedClickable(
    onLongClick: ((UINode, PointerEvent) -> Unit)? = null,
    onDoubleClick: ((UINode, PointerEvent) -> Unit)? = null,
    onClick: ((UINode, PointerEvent) -> Unit)? = null
): Modifier {
    require(onClick != null || onLongClick != null || onDoubleClick != null) { "You must specify at least one function" }

    var mod = this

    if (onLongClick != null) {
        var clickStart = 0L

        mod = mod
            .onPointerEvent(PointerEventType.PRESS) { _, _ -> clickStart = System.currentTimeMillis() }
            .onPointerEvent(PointerEventType.RELEASE) { node, event ->
                if (clickStart != 0L && (System.currentTimeMillis() - clickStart) > LONG_CLICK_THRESHOLD) {
                    clickStart = 0L
                    onLongClick(node, event)
                }
            }
    }
    if (onDoubleClick != null) {
        var clickStart = 0L

        mod = mod.onPointerEvent(PointerEventType.PRESS) { node, event ->
            if (clickStart != 0L && (System.currentTimeMillis() - clickStart) < DOUBLE_CLICK_THRESHOLD) {
                clickStart = 0L
                return@onPointerEvent onDoubleClick(node, event)
            }

            clickStart = System.currentTimeMillis()
        }
    }
    if (onClick != null) mod = mod.onPointerEvent(PointerEventType.RELEASE, onClick)

    return mod
}