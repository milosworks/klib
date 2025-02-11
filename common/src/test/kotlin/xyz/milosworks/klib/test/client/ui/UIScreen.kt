package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
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
		Surface {
			Column(modifier = Modifier.onHover { true.also { println("hovered column") } }) {
				Slot()
				Text(
					Component.literal("Hello World!"),
					2f,
					modifier = Modifier.onHover { true.also { println("hovered text") } }
				)
				Spacer(
					Modifier.size(50, 50).onClick { true.also { println("first click spacer") } }
						.background(KColor.BLUE).outline(KColor.RED)
						.onClick { true.also { println("clicked spacer") } })
				Texture(
					ResourceLocation.parse("minecraft:textures/block/diamond_block.png"),
					0f,
					0f,
					64,
					64,
					64,
					64,
					Modifier.size(100)
				)
			}
		}
	}

	override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}