package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.util.KColor

enum class GradientDirection {
	TOP_TO_BOTTOM,
	RIGHT_TO_LEFT,
	LEFT_TO_RIGHT,
	BOTTOM_TO_TOP,
}

/**
 * A [Modifier] that applies a background gradient or a solid color to a composable.
 *
 * You can use this modifier to fill a component with a background gradient defined by two colors,
 * or with a solid color if both colors are the same. The direction of the gradient can also be customized
 * to suit your design needs. If the start and end colors are identical, it behaves as a solid color fill.
 *
 * Example usage:
 * ```
 * Modifier.background(KColor.RED) // Solid color background
 * Modifier.background(KColor.RED, KColor.BLUE) // Gradient from red to blue
 * Modifier.background(KColor.RED, KColor.BLUE, GradientDirection.LEFT_TO_RIGHT) // Left-to-right gradient
 * ```
 *
 * @param startColor The starting color of the gradient. If used with the same color for `endColor`, it will create a solid color background.
 * @param endColor The ending color of the gradient. If this color is the same as `startColor`, the result will be a solid color background.
 * @param gradientDirection The direction of the gradient. Default is [GradientDirection.TOP_TO_BOTTOM].
 */
data class BackgroundModifier(
	val startColor: Int,
	val endColor: Int,
	val gradientDirection: GradientDirection = GradientDirection.TOP_TO_BOTTOM
) : Modifier.Element<BackgroundModifier> {
	override fun mergeWith(other: BackgroundModifier): BackgroundModifier =
		throw UnsupportedOperationException("not implemented")

	override fun toString(): String =
		if (startColor == endColor) "BackgroundModifier(color=#${
			String.format(
				"%08X",
				startColor
			)
		}${if (gradientDirection != GradientDirection.TOP_TO_BOTTOM) ", gradientDirection=$gradientDirection" else ""})"
		else "BackgroundModifier(startColor=#${
			String.format(
				"%08X",
				startColor
			)
		}, endColor=#${
			String.format(
				"%08X",
				endColor
			)
		}${if (gradientDirection != GradientDirection.TOP_TO_BOTTOM) ", gradientDirection=$gradientDirection" else ""})"
}

/**
 * Applies a solid background color to a composable.
 *
 * This modifier fills the composable with a single color.
 *
 * @param color The color to fill the background.
 */
@Stable
fun Modifier.background(color: KColor): Modifier = this then BackgroundModifier(color.argb, color.argb)

/**
 * Applies a gradient background to a composable.
 *
 * This modifier fills the composable with a gradient that transitions from the `startColor` to the `endColor`.
 * If the `startColor` and `endColor` are the same, this will act as a solid color background.
 *
 * @param startColor The starting color of the gradient.
 * @param endColor The ending color of the gradient.
 */
@Stable
fun Modifier.background(startColor: KColor, endColor: KColor): Modifier =
	this then BackgroundModifier(startColor.argb, endColor.argb)

/**
 * Applies a gradient background with customizable direction to a composable.
 *
 * This modifier fills the composable with a gradient that transitions from the `startColor` to the `endColor`.
 * The direction of the gradient can be customized by specifying one of the [GradientDirection] values.
 *
 * @param startColor The starting color of the gradient.
 * @param endColor The ending color of the gradient.
 * @param gradientDirection The direction of the gradient. Default is [GradientDirection.TOP_TO_BOTTOM].
 */
@Stable
fun Modifier.background(
	startColor: KColor,
	endColor: KColor,
	gradientDirection: GradientDirection = GradientDirection.TOP_TO_BOTTOM
): Modifier = this then BackgroundModifier(startColor.argb, endColor.argb, gradientDirection)


/**
 * Applies a solid background color to a composable.
 *
 * This modifier fills the composable with a single color.
 *
 * @param color The color to fill the background.
 * @return A [Modifier] with the solid background color applied.
 */
@Stable
fun Modifier.background(color: Int): Modifier = this then BackgroundModifier(color, color)

/**
 * Applies a gradient background to a composable.
 *
 * This modifier fills the composable with a gradient that transitions from the `startColor` to the `endColor`.
 * If the `startColor` and `endColor` are the same, this will act as a solid color background.
 *
 * @param startColor The starting color of the gradient.
 * @param endColor The ending color of the gradient.
 */
@Stable
fun Modifier.background(startColor: Int, endColor: Int): Modifier =
	this then BackgroundModifier(startColor, endColor)

/**
 * Applies a gradient background with customizable direction to a composable.
 *
 * This modifier fills the composable with a gradient that transitions from the `startColor` to the `endColor`.
 * The direction of the gradient can be customized by specifying one of the [GradientDirection] values.
 *
 * @param startColor The starting color of the gradient.
 * @param endColor The ending color of the gradient.
 * @param gradientDirection The direction of the gradient. Default is [GradientDirection.TOP_TO_BOTTOM].
 */
@Stable
fun Modifier.background(
	startColor: Int,
	endColor: Int,
	gradientDirection: GradientDirection = GradientDirection.TOP_TO_BOTTOM
): Modifier = this then BackgroundModifier(startColor, endColor, gradientDirection)