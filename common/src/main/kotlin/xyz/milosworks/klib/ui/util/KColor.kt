package xyz.milosworks.klib.ui.util

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.util.Mth
import net.minecraft.world.item.DyeColor
import java.awt.Color
import kotlin.random.Random

operator fun Color.component1(): Int = red
operator fun Color.component2(): Int = green
operator fun Color.component3(): Int = blue
operator fun Color.component4(): Int = alpha

/**
 * Get the decimal representation of this color in rgb
 */
val Color.rgb: Int
	get() = (red * 255) shl 16 or (green * 255) shl 8 or (blue * 255)

/**
 * Get the decimal representation of this color in argb
 */
val Color.argb: Int
	get() = (alpha shl 24) or (red shl 16) or (green shl 8) or blue

/**
 * Temporary kotlin version of [Color].
 * This data class will be removed when kotlin allows static extension functions and we will move to [Color] from java
 */
@Serializable
data class KColor(val red: Int = 0, val green: Int = 0, val blue: Int = 0, val alpha: Int = 255) {
	companion object {
		val WHITE = ofRgb(0xFFFFFF)
		val LIGHT_GRAY = ofRgb(0xC0C0C0)
		val GRAY = ofRgb(0x808080)
		val DARK_GRAY = ofRgb(0x404040)
		val BLACK = ofRgb(0x000000)
		val RED = ofRgb(0xFF0000)
		val PINK = ofRgb(0xFFAFAF)
		val ORANGE = ofRgb(0xFFA500)
		val YELLOW = ofRgb(0xFFFF00)
		val GREEN = ofRgb(0x4CAF50)
		val MAGENTA = ofRgb(0xFF00FF)
		val CYAN = ofRgb(0x00FFFF)
		val LIGHT_BLUE = ofRgb(0x2196F3)
		val BLUE = ofRgb(0x0000FF)

		/**
		 * Create a Color from the decimal version of an argb color.
		 */
		fun ofArgb(argb: Long): KColor {
			return KColor(
				(argb shr 24).toInt(),
				(argb shr 16 and 255).toInt(),
				(argb shr 8 and 255).toInt(),
				(argb and 255).toInt()
			)
		}

		/**
		 * Create a Color from the decimal version of a rgb color.
		 */
		fun ofRgb(rgb: Int): KColor {
			return KColor(
				rgb shr 16 and 255,
				rgb shr 8 and 255,
				rgb and 255
			)
		}

		/**
		 * Create a Color based on the HSV values of a color.
		 */
		fun ofHsv(hue: Float, saturation: Float, value: Float): KColor {
			// owo-lib calls 0.5e-7f the funny number "do not turn a hue value of 1f into yellow" constant.
			return ofRgb(Mth.hsvToRgb(hue - 0.5e-7f, saturation, value))
		}

		/**
		 * Create a Color based on the HSV values of a color.
		 */
		fun ofHsv(hue: Float, saturation: Float, value: Float, alpha: Float): KColor {
			// owo-lib calls 0.5e-7f the funny number "do not turn a hue value of 1f into yellow" constant.
			return ofArgb(((alpha * 255).toInt() shl 24 or Mth.hsvToRgb(hue - 0.5e-7f, saturation, value)).toLong())
		}

		/**
		 * Create a Color based on the color in a [ChatFormatting]
		 */
		fun ofFormatting(formatting: ChatFormatting): KColor {
			return ofRgb(formatting.color ?: 0)
		}

		/**
		 * Create a Color based on the Dye color.
		 */
		fun ofDye(dye: DyeColor): KColor {
			return ofArgb(dye.textureDiffuseColor.toLong())
		}

		/**
		 * Create a random Color
		 */
		fun random(alpha: Boolean = true): KColor {
			return if (alpha) {
				ofArgb((Random.nextLong(0x100000000) or (0xFF000000L)))
			} else {
				ofRgb(Random.nextInt(0x1000000))
			}
		}
	}

	/**
	 * Get the decimal representation of this color in rgb
	 */
	val rgb: Int
		get() = (red * 255) shl 16 or (green * 255) shl 8 or (blue * 255)

	/**
	 * Get the decimal representation of this color in argb
	 */
	val argb: Int
		get() = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
