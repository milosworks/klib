package xyz.milosworks.klib.ui.modifiers.input

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.modifiers.core.Modifier

const val LONG_CLICK_THRESHOLD = 500
const val DOUBLE_CLICK_THRESHOLD = 300

enum class PointerEventType {
    PRESS,
    GLOBAL_PRESS,
    RELEASE,
    GLOBAL_RELEASE,
    MOVE,
    ENTER,
    EXIT,
    SCROLL,
    DRAG,
    GLOBAL_DRAG,
}

data class OnPointerEventModifier<T : UINode>(
    val eventType: PointerEventType,
    val onEvent: (T, PointerEvent) -> Unit
) : Modifier.Element<OnPointerEventModifier<*>> {
    override fun mergeWith(other: OnPointerEventModifier<*>): OnPointerEventModifier<*> = other

    override fun toString(): String = "OnPointerEventModifier(eventType=${eventType.name})"
}

@Stable
fun <T : UINode> Modifier.onPointerEvent(
    type: PointerEventType,
    onEvent: (T, PointerEvent) -> Unit
): Modifier =
    this then OnPointerEventModifier(type, onEvent)

@Suppress("UNCHECKED_CAST")
@Stable
fun <T : UINode> Modifier.onScroll(
    global: Boolean = false,
    onScrollEvent: (T, ScrollEvent) -> Unit
): Modifier =
    this then OnPointerEventModifier(
        if (global) PointerEventType.GLOBAL_PRESS else PointerEventType.SCROLL,
        onScrollEvent as (T, PointerEvent) -> Unit
    )

@Suppress("UNCHECKED_CAST")
@Stable
fun <T : UINode> Modifier.onDrag(
    global: Boolean = false,
    onDragEvent: (T, DragEvent) -> Unit
): Modifier =
    this then OnPointerEventModifier(
        if (global) PointerEventType.GLOBAL_DRAG else PointerEventType.DRAG,
        onDragEvent as (T, PointerEvent) -> Unit
    )

@Stable
fun <T : UINode> Modifier.combinedClickable(
    onLongClick: ((T, PointerEvent) -> Unit)? = null,
    onDoubleClick: ((T, PointerEvent) -> Unit)? = null,
    onClick: ((T, PointerEvent) -> Unit)? = null
): Modifier {
    require(onClick != null || onLongClick != null || onDoubleClick != null) { "You must specify at least one function" }

    var mod = this

    if (onLongClick != null) {
        var clickStart = 0L

        mod = mod
            .onPointerEvent<T>(PointerEventType.PRESS) { _, _ ->
                clickStart = System.currentTimeMillis()
            }
            .onPointerEvent<T>(PointerEventType.RELEASE) { node, event ->
                if (clickStart != 0L && (System.currentTimeMillis() - clickStart) > LONG_CLICK_THRESHOLD) {
                    clickStart = 0L
                    onLongClick(node, event)
                }
            }
    }
    if (onDoubleClick != null) {
        var clickStart = 0L

        mod = mod.onPointerEvent<T>(PointerEventType.PRESS) { node, event ->
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