package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Alignment
import xyz.milosworks.klib.ui.layout.BoxMeasurePolicy
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.nodes.UINode

/**
 * Creates a dark Minecraft panel with a nine-patch texture.
 *
 * This composable is typically used to wrap content such as a Column, Box, or Row.
 * By default, the size of the panel adjusts to its children, but you can also
 * specify the panel's size manually.
 *
 * Example usage:
 * ```
 * DarkSurface {
 *     Column {
 *         Text("Content inside the dark panel")
 *         Text("Another piece of content")
 *     }
 * }
 * ```
 */
@Composable
fun DarkSurface(
	modifier: Modifier = Modifier,
	contentAlignment: Alignment = Alignment.TopStart,
	content: @Composable () -> Unit
) = Surface(KLib["panel/dark"], contentAlignment, modifier, content)

/**
 * Creates a Minecraft panel with a nine-patch texture.
 *
 * Like `DarkSurface`, this composable wraps content, typically used with
 * a Column, Box or Row. The panel size is dynamic by default, but can be
 * controlled explicitly.
 *
 * @param texture The nine-patch texture to use for this panel. For more information,
 * see the [ninePatchTexture] documentation.
 *
 * Example usage:
 * ```
 * Surface {
 *     Column {
 *         Text("Content inside the normal panel")
 *         Text("Another piece of content")
 *     }
 * }
 * ```
 */
@Composable
fun Surface(
	texture: ResourceLocation = KLib["panel/normal"],
	contentAlignment: Alignment = Alignment.TopStart,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	val measurePolicy = remember(contentAlignment) { BoxMeasurePolicy(contentAlignment) }
	Layout(
		measurePolicy,
		object : Renderer {
			override fun render(
				node: UINode,
				x: Int,
				y: Int,
				guiGraphics: GuiGraphics,
				mouseX: Int,
				mouseY: Int,
				partialTick: Float
			) {
				guiGraphics.ninePatchTexture(x, y, node.width, node.height, texture)
			}
		},
		Modifier.debug(texture.toString()) then modifier,
		content
	)
}