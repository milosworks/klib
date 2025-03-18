package xyz.milosworks.klib.ui.modifiers.offset

import xyz.milosworks.klib.ui.layout.IntOffset
import xyz.milosworks.klib.ui.layout.Placeable

class OffsetPlaceable(
    val offset: IntOffset,
    val inner: Placeable
) : Placeable by inner {
    override fun placeAt(x: Int, y: Int) {
    }
}