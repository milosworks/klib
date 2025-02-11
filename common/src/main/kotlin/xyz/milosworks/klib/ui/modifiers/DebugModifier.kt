package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import net.minecraft.network.chat.Component

/**
 * A modifier that adds additional debug information to a composable for inspection in debug mode.
 *
 * This modifier allows you to attach custom strings or components to a composable, which can be helpful
 * for debugging purposes. When enabled in a debug environment, these details are shown in the logs or UI,
 * offering more insight into the state of the composable.
 *
 * @param strs A list of strings that will be included as debug information.
 * @param comps A list of components that will be appended as debug information.
 */
data class DebugModifier(
	val strs: List<String> = emptyList(),
	val comps: List<Component> = emptyList()
) : Modifier.Element<DebugModifier> {
	override fun mergeWith(other: DebugModifier): DebugModifier =
		DebugModifier(
			strs = this.strs + other.strs,
			comps = this.comps + other.comps
		)

	override fun toString(): String = strs.joinToString(", ").ifEmpty { super.toString() }

	override fun toComponent(): Component = Component.empty().apply {
		strs.map { Component.literal(it) }.forEach { append(it) }
		comps.forEach { append(it) }
	}.takeIf { it != Component.empty() } ?: Component.literal(super.toString())

	fun toComponents(): List<Component> = (strs.map { Component.literal(it) } + comps).ifEmpty {
		listOf(Component.literal(super.toString()))
	}

}

/**
 * Adds debug strings to a composable for inspection.
 *
 * This modifier appends custom strings to the composable's debug information.
 * These strings can be displayed in debug mode for further analysis.
 *
 * @param strs The strings to be added to the debug information.
 */
@Stable
fun Modifier.debug(vararg strs: String): Modifier = this then DebugModifier(strs = strs.toList())

/**
 * Adds debug components to a composable for inspection.
 *
 * This modifier appends custom [Component]'s to the composable's debug information.
 * These components can be displayed in debug mode for further analysis.
 *
 * @param comps The components to be added to the debug information.
 */
@Stable
fun Modifier.debug(vararg comps: Component): Modifier = this then DebugModifier(comps = comps.toList())
