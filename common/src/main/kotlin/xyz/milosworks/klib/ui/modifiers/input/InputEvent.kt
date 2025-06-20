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

sealed class PointerEvent(
    val type: PointerEventType,
    val mouseX: Double,
    val mouseY: Double,
) : InputEvent()

class BasicPointerEvent(
    type: PointerEventType,
    mouseX: Double,
    mouseY: Double,
) : PointerEvent(type, mouseX, mouseY)

class ScrollEvent(
    mouseX: Double,
    mouseY: Double,
    val scrollX: Double,
    val scrollY: Double,
) : PointerEvent(PointerEventType.SCROLL, mouseX, mouseY)

class DragEvent(
    mouseX: Double,
    mouseY: Double,
    val button: Int,
    val dragX: Double,
    val dragY: Double,
) : PointerEvent(PointerEventType.DRAG, mouseX, mouseY)

data class KeyEvent(
    val keyCode: Int,
    val scanCode: Int,
    val modifiers: Int
) : InputEvent()

data class CharEvent(
    val codePoint: Char,
    val modifiers: Int
) : InputEvent()