package xyz.milosworks.klib.test.client.ui

import androidx.compose.runtime.Composable
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import xyz.milosworks.klib.ui.ComposeContainerScreen
import xyz.milosworks.klib.ui.components.Slot
import xyz.milosworks.klib.ui.layout.Column
import xyz.milosworks.klib.ui.layout.Row

class UIScreen(menu: UIMenu, inventory: Inventory, title: Component) :
	ComposeContainerScreen<UIMenu>(menu, inventory, title) {

	private val rows = 10

	init {
		this.imageHeight = 114 + rows * 18
		this.inventoryLabelY = this.imageHeight - 94
		start {
			content()
		}
	}

	@Composable
	fun content() {
		Column {
			for (i in 0 until rows) {
				Row {
					for (j in 0 until 9) {
						Slot()
					}
				}
			}
		}
	}
}