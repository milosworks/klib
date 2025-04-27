package xyz.milosworks.klib.ui.modifiers.margin

import xyz.milosworks.klib.ui.layout.IntOffset

data class OutsetValues(
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0,
) {
    fun getOffset() = IntOffset(left, top)
}