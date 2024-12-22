package xyz.milosworks.klib.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

/**
 * Helper class for block registration with optional block item creation.
 *
 * ### Example
 * ```kotlin
 * object TBlocks : BlockRegistryHelper<Block>(TestMod.ID) {
 *     val TEST by block("test") { Test(BlockBehaviour.Properties.of()) }
 * }
 * ```
 * @param modId The ID of your mod
 */
@Suppress("UNCHECKED_CAST")
open class BlockRegistryHelper<T : Block>(modId: String) : RegistryHelper<T>(
	DeferredRegister.create(modId, Registries.BLOCK) as DeferredRegister<T>
) {
	open val itemRegistry = DeferredRegister.create(modId, Registries.ITEM)

	override fun init() = super.init().also { itemRegistry.register() }

	/**
	 * Register a new block
	 *
	 * @param id The id of the new block
	 * @param itemSupplier The optional supplier of the [BlockItem] for this Block
	 * @param supplier A factory for the new entry. The factory should not cache the created entry.
	 */
	open fun <V : T> block(
		id: String,
		itemSupplier: ((V, Item.Properties) -> BlockItem)? = { block, props -> BlockItem(block, props) },
		supplier: () -> V
	): RegistrySupplier<V> {
		val holder = register(id, supplier)

		itemRegistry.register(id) { ->
			val block = holder.get()

			return@register itemSupplier?.invoke(block, Item.Properties()) ?: BlockItem(block, Item.Properties())
		}

		return holder
	}
}