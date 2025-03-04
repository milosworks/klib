package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.TextureModifier
import xyz.milosworks.klib.ui.modifiers.firstOrNull
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.nodes.UINode


@Composable
fun Button(text: Component? = null, active: Boolean = true, modifier: Modifier, onClick: (UINode) -> Unit) {
	val texture: ResourceLocation by remember {
		mutableStateOf(
			modifier.firstOrNull<TextureModifier>()?.texture ?: KLib["button"]
		)
	}
	var hovered: Boolean by remember { mutableStateOf(false) }

	Layout(
		measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
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
				guiGraphics.ninePatchTexture(
					x, y, node.width, node.height,
					if (!active) texture.withSuffix(TextureStates.DISABLED)
					else if (hovered) texture.withSuffix(TextureStates.HOVERED)
					else texture.withSuffix(TextureStates.DEFAULT)
				)
			}
		},
		modifier = Modifier
			.onPointerEvent(PointerEventType.ENTER) { _, e ->
				hovered = true

				GLFW.glfwSetCursor(
					Minecraft.getInstance().window.window,
					GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
				)

				e.consume()
			}
			.onPointerEvent(PointerEventType.EXIT) { _, e ->
				hovered = false

				GLFW.glfwSetCursor(
					Minecraft.getInstance().window.window,
					GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
				)

				e.consume()
			}
			.onPointerEvent(PointerEventType.PRESS) { node, e -> if (active) onClick(node); e.consume() }
				then modifier,
	) {
		if (text != null) Text(text)
	}
}