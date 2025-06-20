package xyz.milosworks.klib.ui.modifiers.position.outset

import xyz.milosworks.klib.ui.layout.primitive.IntCoordinates

data class OutsetValues(
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0,
) {
    fun getOffset() = IntCoordinates(left, top)
}