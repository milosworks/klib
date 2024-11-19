package vyrek.kodek.init

import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import vyrek.kodek.TestMod
import vyrek.kodek.block.*
import net.minecraft.world.item.Item.Properties as ItemProperties
import net.minecraft.world.level.block.state.BlockBehaviour.Properties.of as props

object TBlocks {
	private val BLOCKS: DeferredRegister<Block> = DeferredRegister.createBlocks(TestMod.ID)

	val TEST by block("test") { Test(props()) }

	private fun <T : Block> block(
		id: String,
		itemSupplier: ((T, ItemProperties) -> BlockItem)? = { block, props -> BlockItem(block, props) },
		supplier: () -> T
	): DeferredHolder<Block, T> {
		val blockHolder = BLOCKS.register(id, supplier)

		TItems.ITEMS.register(id) { ->
			val block = blockHolder.get()

			return@register itemSupplier?.invoke(block, ItemProperties()) ?: BlockItem(block, ItemProperties())
		}

		return blockHolder
	}

	fun init(event: IEventBus) {
		BLOCKS.register(event)
	}
}