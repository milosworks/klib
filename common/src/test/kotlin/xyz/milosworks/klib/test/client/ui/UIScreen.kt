package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import xyz.milosworks.klib.ui.ComposeContainerScreen
import xyz.milosworks.klib.ui.KColor
import xyz.milosworks.klib.ui.components.Slot
import xyz.milosworks.klib.ui.components.Texture
import xyz.milosworks.klib.ui.layout.Column
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.background
import xyz.milosworks.klib.ui.modifiers.size

class UIScreen(menu: UIMenu, inventory: Inventory, title: Component) :
	ComposeContainerScreen<UIMenu>(menu, inventory, title) {

	init {
//		this.imageHeight = 114 + 10 * 18
//		this.inventoryLabelY = this.imageHeight - 94
		start {
			content()
		}
	}

	@Composable
	fun content() {
		Column(Modifier.background(KColor.GREEN)) {
			Slot()
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
//			Spacer(Modifier.size(50, 50).background(KColor.BLUE).outline(KColor.RED))
		}
	}

	override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}