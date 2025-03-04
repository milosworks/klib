package xyz.milosworks.klib.ui.modifiers.input

sealed class InputEvent {
	internal var isConsumed: Boolean = false
		private set
	internal var bypassSuper: Boolean = false
		private set

	/**
	 * Consumes the event
	 *
	 * @param bypassSuperCall If true, later the event handler will return
	 * this flag, OR the super implementation result.
	 */
	fun consume(bypassSuperCall: Boolean = false) {
		isConsumed = true
		bypassSuper = bypassSuperCall
	}
}

data class PointerEvent(
	val type: PointerEventType,
	val mouseX: Double,
	val mouseY: Double,
) : InputEvent()

data class KeyEvent(
	val keyCode: Int,
	val scanCode: Int,
	val modifiers: Int
) : InputEvent()

data class CharEvent(
	val codePoint: Char,
	val modifiers: Int
) : InputEvent()