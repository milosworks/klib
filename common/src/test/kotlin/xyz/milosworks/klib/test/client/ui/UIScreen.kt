package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import xyz.milosworks.klib.ui.ComposeContainerScreen
import xyz.milosworks.klib.ui.components.Panel
import xyz.milosworks.klib.ui.components.Slot
import xyz.milosworks.klib.ui.components.Spacer
import xyz.milosworks.klib.ui.components.Texture
import xyz.milosworks.klib.ui.layout.Column
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.background
import xyz.milosworks.klib.ui.modifiers.outline
import xyz.milosworks.klib.ui.modifiers.size
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
		Panel {
			Column {
				Slot()
				Spacer(Modifier.size(50, 50).background(KColor.BLUE).outline(KColor.RED))
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