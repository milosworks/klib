package xyz.milosworks.ktest.ui

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import xyz.milosworks.ktest.init.TMenus

class UIMenu(containerId: Int, inventory: Inventory) :
    AbstractContainerMenu(TMenus.UI.get(), containerId) {
//	companion object {
//		fun getProvider(id: Int, player: Player): UIMenu =
//			UIMenu(id, player)
//	}

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY
    override fun stillValid(player: Player): Boolean = true
}