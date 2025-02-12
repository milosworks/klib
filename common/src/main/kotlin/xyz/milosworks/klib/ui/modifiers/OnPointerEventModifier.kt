package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.nodes.UINode

enum class PointerEventType {
	PRESS,
	RELEASE,
	MOVE,
	ENTER,
	EXIT,
	SCROLL
}

data class OnPointerEventModifier(
	val eventType: PointerEventType, val onEvent: (UINode) -> Boolean
) : Modifier.Element<OnPointerEventModifier> {
	override fun mergeWith(other: OnPointerEventModifier): OnPointerEventModifier = other
}

@Stable
fun Modifier.onPointerEvent(type: PointerEventType, onEvent: (UINode) -> Boolean): Modifier =
	this then OnPointerEventModifier(type, onEvent)

@Stable
fun Modifier.combinedClickable(
	onLongClick: ((UINode) -> Boolean)? = null,
	onDoubleClick: ((UINode) -> Boolean)? = null,
	onClick: ((UINode) -> Boolean)? = null
): Modifier {
	require(onClick != null || onLongClick != null || onDoubleClick != null) { "You must specify at least one function" }

	var mod = this

	if (onLongClick != null) {
		var clickStart = 0L

		mod = mod.onPointerEvent(PointerEventType.PRESS) {
			clickStart = System.currentTimeMillis()
			false
		}.onPointerEvent(PointerEventType.RELEASE) {
			if (clickStart != 0L && (System.currentTimeMillis() - clickStart) > 500) {
				clickStart = 0L
				return@onPointerEvent onLongClick(it)
			}

			false
		}
	}
	if (onDoubleClick != null) {
		var clickStart = 0L

		mod = mod.onPointerEvent(PointerEventType.PRESS) {
			if (clickStart != 0L && (System.currentTimeMillis() - clickStart) < 300) {
				clickStart = 0L
				return@onPointerEvent onDoubleClick(it)
			}

			clickStart = System.currentTimeMillis()
			false
		}
	}
	if (onClick != null) mod = mod.onPointerEvent(PointerEventType.RELEASE, onClick)

	return mod
}