package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Text(modifier: Modifier = Modifier) {
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
				
			}
		},
		modifier = modifier,
	)
}