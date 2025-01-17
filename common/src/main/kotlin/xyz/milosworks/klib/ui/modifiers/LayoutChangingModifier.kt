package xyz.milosworks.klib.ui.modifiers

import xyz.milosworks.klib.ui.layout.IntOffset
import xyz.milosworks.klib.ui.layout.IntSize

interface LayoutChangingModifier {
	fun modifyPosition(offset: IntOffset): IntOffset = offset

	/** Modify constraints as they appear to parent nodes laying out this node. */
	fun modifyLayoutConstraints(measuredSize: IntSize, constraints: Constraints): Constraints =
		modifyInnerConstraints(constraints)

	/** Modify constraints as they appear to this node and its children for layout. */
	fun modifyInnerConstraints(constraints: Constraints): Constraints = constraints
}