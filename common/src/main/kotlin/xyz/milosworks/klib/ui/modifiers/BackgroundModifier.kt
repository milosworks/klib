package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.KColor

enum class GradientDirection {
	TOP_TO_BOTTOM,
	RIGHT_TO_LEFT,
	LEFT_TO_RIGHT,
	BOTTOM_TO_TOP,
}

data class BackgroundModifier(
	val startColor: Int,
	val endColor: Int,
	val gradientDirection: GradientDirection = GradientDirection.TOP_TO_BOTTOM
) : Modifier.Element<BackgroundModifier> {
	constructor(color: Int) : this(color, color)
	constructor(color: KColor) : this(color, color)
	constructor(startColor: KColor, endColor: KColor) : this(startColor.argb, endColor.argb)

	override fun mergeWith(other: BackgroundModifier): BackgroundModifier =
		throw UnsupportedOperationException("not implemented")
}

@Stable
fun Modifier.background(color: KColor): Modifier = this then BackgroundModifier(color, color)

@Stable
fun Modifier.background(startColor: KColor, endColor: KColor): Modifier =
	this then BackgroundModifier(startColor, endColor)