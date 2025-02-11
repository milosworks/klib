package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Button(modifier: Modifier, onClick: (UINode) -> Unit) {
	val texture = modifier.firstOrNull<TextureModifier>()?.texture ?: KLib["button"]
	var active = false
	var hovered = false

	Layout(
		measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
		renderer = object : DefaultRenderer() {
			override fun render(
				uiNode: UINode,
				x: Int,
				y: Int,
				guiGraphics: GuiGraphics,
				mouseX: Int,
				mouseY: Int,
				partialTick: Float
			) {
				super.render(uiNode, x, y, guiGraphics, mouseX, mouseY, partialTick)
			}
		},
		modifier = Modifier.onClick {
			onClick(it)
			true
		}.onHover {
			hovered = true
			true
		} then modifier,
	)
}