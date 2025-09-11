package xyz.milosworks.klib.ui.modifiers.position.padding

import xyz.milosworks.klib.ui.layout.primitive.IntCoordinates

data class PaddingValues(
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0,
) {
    fun getOffset() = IntCoordinates(left, top)

    operator fun plus(other: PaddingValues) = PaddingValues(
        left + other.left,
        right + other.right,
        top + other.top,
        bottom + other.bottom
    )
}