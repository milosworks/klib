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
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.nodes.UINode

const val DEFAULT_TEXTURE = "/default"
const val DISABLED_TEXTURE = "/disabled"
const val HOVERED_TEXTURE = "/hovered"

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
					if (!active) texture.withSuffix(DISABLED_TEXTURE)
					else if (hovered) texture.withSuffix(HOVERED_TEXTURE)
					else texture.withSuffix(DEFAULT_TEXTURE)
				)
			}
		},
		modifier = Modifier
			.onPointerEvent(PointerEventType.ENTER) { _, _, _ ->
				hovered = true

				GLFW.glfwSetCursor(
					Minecraft.getInstance().window.window,
					GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
				)

				true
			}
			.onPointerEvent(PointerEventType.EXIT) { _, _, _ ->
				hovered = false

				GLFW.glfwSetCursor(
					Minecraft.getInstance().window.window,
					GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
				)

				true
			}
			.onPointerEvent(PointerEventType.PRESS) { node, _, _ -> if (active) onClick(node); true }
				then modifier,
	) {
		if (text != null) Text(text)
	}
}