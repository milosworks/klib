package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Slot(modifier: Modifier = Modifier) {
	Layout(
		measurePolicy = { _, constraints ->
			MeasureResult(constraints.minWidth, constraints.minHeight) {}
		},
		renderer = object : Renderer {
			private val SLOT = KLib["textures/gui/slot.png"]
			override fun render(
				node: UINode,
				x: Int,
				y: Int,
				guiGraphics: GuiGraphics,
				mouseX: Int,
				mouseY: Int,
				partialTick: Float
			) {
				guiGraphics.blit(SLOT, x, y, 18, 18, 0f, 0f, 18, 18, 18, 18)
			}
		},
		modifier = Modifier.sizeIn(minWidth = 18, minHeight = 18).then(modifier)
	)
}