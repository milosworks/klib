package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.util.KColor

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
	override fun mergeWith(other: BackgroundModifier): BackgroundModifier =
		throw UnsupportedOperationException("not implemented")
}

@Stable
fun Modifier.background(color: KColor): Modifier = this then BackgroundModifier(color.argb, color.argb)

@Stable
fun Modifier.background(startColor: KColor, endColor: KColor): Modifier =
	this then BackgroundModifier(startColor.argb, endColor.argb)

@Stable
fun Modifier.background(color: Int): Modifier = this then BackgroundModifier(color, color)