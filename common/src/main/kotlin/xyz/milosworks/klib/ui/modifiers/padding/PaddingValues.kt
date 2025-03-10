package xyz.milosworks.klib.ui.modifiers.padding

import xyz.milosworks.klib.ui.layout.IntOffset

data class PaddingValues(
    val start: Int = 0,
    val end: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0,
) {
    fun getOffset() = IntOffset(start, top)
}