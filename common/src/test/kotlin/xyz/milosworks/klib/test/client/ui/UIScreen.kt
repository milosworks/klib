package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import xyz.milosworks.klib.ui.ComposeContainerScreen
import xyz.milosworks.klib.ui.components.*
import xyz.milosworks.klib.ui.layout.Column
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.util.KColor

class UIScreen(menu: UIMenu, inventory: Inventory, title: Component) :
	ComposeContainerScreen<UIMenu>(menu, inventory, title) {

	init {
		start {
			content()
		}
	}

	@Composable
	fun content() {
		var active: Boolean by remember { mutableStateOf(false) }
		var text: String by remember { mutableStateOf("") }

		Surface {
			Column(
				modifier = Modifier.onPointerEvent(PointerEventType.SCROLL) { _, _, _ -> println("scroll column"); true }
			) {
				Slot()
				Text(
					Component.literal("Hello World!"),
					2f,
					modifier = Modifier
						.onPointerEvent(PointerEventType.ENTER) { _, _, _ -> println("enter text"); true }
						.onPointerEvent(PointerEventType.EXIT) { _, _, _ -> println("exit text"); true }
				)
				TextField(
					text,
					{ text = it },
					modifier = Modifier.size(90, 18)
				)
				Spacer(
					Modifier.size(50, 50)
						.background(KColor.BLUE).outline(KColor.RED)
						.combinedClickable(
							onLongClick = { _, _, _ -> println("onLongClick spacer"); false },
							onDoubleClick = { _, _, _ -> println("onDoubleClick spacer"); false },
							onClick = { _, _, _ -> println("onClick spacer"); false }
						)
				)
				Button(modifier = Modifier.size(20, 20)) { println("button clicked"); active = (active == false) }
				Texture(
					ResourceLocation.parse("minecraft:textures/block/diamond_block.png"),
					0f,
					0f,
					64,
					64,
					64,
					64,
					Modifier.size(100)
						.onPointerEvent(PointerEventType.MOVE) { _, _, _ -> println("moved texture"); true }
				)


				if (active) Text(Component.literal("Clicked Button!"))
			}
		}
	}

	override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}