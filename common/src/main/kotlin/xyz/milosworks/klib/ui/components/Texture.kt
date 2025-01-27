package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.SizeModifier
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Texture(
	loc: ResourceLocation,
	uOffset: Float,
	vOffset: Float,
	u: Int,
	v: Int,
	textureWidth: Int,
	textureHeight: Int,
	modifier: Modifier = Modifier
) {
	var size: SizeModifier? = null

	modifier.foldIn(Unit) { _, element ->
		when (element) {
			is SizeModifier -> size = element
		}
	}

	if (size == null) throw IllegalStateException("You need to specify a SizeModifier first.")

	Layout(
		measurePolicy = { _, constraints ->
			MeasureResult(constraints.minWidth, constraints.minHeight) {}
		},
		renderer = object : Renderer {
			override fun render(
				node: UINode,
				x: Int,
				y: Int,
				guiGraphics: GuiGraphics,
				mouseX: Int,
				mouseY: Int,
				partialTick: Float
			) {
				guiGraphics.blit(
					loc,
					x,
					y,
					size.constraints.minWidth,
					size.constraints.minHeight,
					uOffset,
					vOffset,
					u,
					v,
					textureWidth,
					textureHeight
				)
			}
		},
		modifier = modifier,
	)
}