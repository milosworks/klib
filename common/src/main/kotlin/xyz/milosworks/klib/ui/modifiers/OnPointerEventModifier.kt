package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.nodes.UINode

const val LONG_CLICK_THRESHOLD = 500
const val DOUBLE_CLICK_THRESHOLD = 300

enum class PointerEventType {
	PRESS,
	RELEASE,
	MOVE,
	ENTER,
	EXIT,
	SCROLL
}

data class OnPointerEventModifier(
	val eventType: PointerEventType, val onEvent: (UINode, x: Double, y: Double) -> Boolean
) : Modifier.Element<OnPointerEventModifier> {
	override fun mergeWith(other: OnPointerEventModifier): OnPointerEventModifier = other
}

@Stable
fun Modifier.onPointerEvent(type: PointerEventType, onEvent: (UINode, x: Double, y: Double) -> Boolean): Modifier =
	this then OnPointerEventModifier(type, onEvent)

@Stable
fun Modifier.combinedClickable(
	onLongClick: ((UINode, x: Double, y: Double) -> Boolean)? = null,
	onDoubleClick: ((UINode, x: Double, y: Double) -> Boolean)? = null,
	onClick: ((UINode, x: Double, y: Double) -> Boolean)? = null
): Modifier {
	require(onClick != null || onLongClick != null || onDoubleClick != null) { "You must specify at least one function" }

	var mod = this

	if (onLongClick != null) {
		var clickStart = 0L

		mod = mod.onPointerEvent(PointerEventType.PRESS) { _, _, _ ->
			clickStart = System.currentTimeMillis()
			false
		}.onPointerEvent(PointerEventType.RELEASE) { node, x, y ->
			if (clickStart != 0L && (System.currentTimeMillis() - clickStart) > LONG_CLICK_THRESHOLD) {
				clickStart = 0L
				return@onPointerEvent onLongClick(node, x, y)
			}

			false
		}
	}
	if (onDoubleClick != null) {
		var clickStart = 0L

		mod = mod.onPointerEvent(PointerEventType.PRESS) { node, x, y ->
			if (clickStart != 0L && (System.currentTimeMillis() - clickStart) < DOUBLE_CLICK_THRESHOLD) {
				clickStart = 0L
				return@onPointerEvent onDoubleClick(node, x, y)
			}

			clickStart = System.currentTimeMillis()
			false
		}
	}
	if (onClick != null) mod = mod.onPointerEvent(PointerEventType.RELEASE, onClick)

	return mod
}