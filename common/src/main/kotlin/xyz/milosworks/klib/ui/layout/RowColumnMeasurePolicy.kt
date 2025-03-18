package xyz.milosworks.klib.ui.layout

import xyz.milosworks.klib.ui.modifiers.Constraints

abstract class RowColumnMeasurePolicy(
    val sumWidth: Boolean = false,
    val sumHeight: Boolean = false,
    val arrangementSpacing: Int = 0,
) : MeasurePolicy {
    override fun measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        var remainingConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { element ->
            val measured = element.measure(remainingConstraints)
            remainingConstraints = remainingConstraints.copy(
                maxWidth = if (sumWidth) (remainingConstraints.maxWidth - measured.width).coerceAtLeast(0) else remainingConstraints.maxWidth,
                maxHeight = if (sumHeight) (remainingConstraints.maxHeight - measured.height).coerceAtLeast(0) else remainingConstraints.maxHeight,
            )
            measured
        }
        val extraSpacing = (arrangementSpacing * (placeables.size - 1)).coerceAtLeast(0)
        val width = if (sumWidth) {
            placeables.sumOf { it.width } + extraSpacing
        } else placeables.maxOfOrNull { it.width } ?: 0

        val height = if (sumHeight) {
            placeables.sumOf { it.height } + extraSpacing
        } else placeables.maxOfOrNull { it.height } ?: 0
        return placeChildren(placeables, maxOf(width, constraints.minWidth), maxOf(height, constraints.minHeight))
    }

    abstract fun placeChildren(placeables: List<Placeable>, width: Int, height: Int): MeasureResult
}