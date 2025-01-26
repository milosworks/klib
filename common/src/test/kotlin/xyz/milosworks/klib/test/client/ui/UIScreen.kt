package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import xyz.milosworks.klib.ui.ComposeContainerScreen
import xyz.milosworks.klib.ui.KColor
import xyz.milosworks.klib.ui.components.Slot
import xyz.milosworks.klib.ui.components.Spacer
import xyz.milosworks.klib.ui.layout.Column
import xyz.milosworks.klib.ui.layout.Row
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.background
import xyz.milosworks.klib.ui.modifiers.outline
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
		Column {
			for (i in 0 until 3) {
				Row {
					for (j in 0 until 9) {
						Slot()
					}
				}
			}
			Spacer(Modifier.size(10, 10))
			Spacer(Modifier.size(50, 50).background(KColor.BLUE).outline(KColor.RED))
		}
	}

	override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {}
}