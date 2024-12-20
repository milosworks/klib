package xyz.milosworks.klib.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import kotlin.reflect.KProperty

/**
 * Helper class for managing registry entries.
 *
 * **Note:** Use `by` when declaring values to avoid `Registry is frozen` errors.
 *
 * ### Example
 * ```kotlin
 * object TCreativeTabs : RegistryHelper<CreativeModeTab>(
 *     DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, TestMod.ID)
 * ) {
 *     val TAB by register("tab") {
 *         CreativeModeTab.builder()
 *             .title(Component.literal("Test"))
 *             .icon { ItemStack(TBlocks.TEST) }
 *             .displayItems { _, output -> output.accept(TBlocks.TEST) }
 *             .build()
 *     }
 * }
 * ```
 *
 * @property registry The registry to where the entries will be registered to
 */
open class RegistryHelper<T : Any>(val registry: DeferredRegister<T>) {
	open fun init() = registry.register()

	/**
	 * Registers a new entry returning a [DeferredHolder]
	 *
	 * @param id The new entry's name. It will automatically have the namespace prefixed.
	 * @param supplier A factory for the new entry. The factory should not cache the created entry.
	 */
	open fun <V : T> register(id: String, supplier: () -> V): RegistrySupplier<V> = registry.register(id, supplier)

	/**
	 * Registers a new entry returning a [RegistrySupplier]
	 *
	 * @param id The new entry's name. It will automatically have the namespace prefixed.
	 * @param supplier A factory for the new entry. The factory should not cache the created entry.
	 */
	open fun <V : T> register(id: ResourceLocation, supplier: () -> V): RegistrySupplier<V> =
		registry.register(id, supplier)

	operator fun <V : T> RegistrySupplier<V>.getValue(any: Any?, property: KProperty<*>): V = get()
}

/**
 * Helper class for block registration with optional block item creation.
 *
 * ### Example
 * ```kotlin
 * object TBlocks : BlockRegistryHelper<Block>(TestMod.ID) {
 *     val TEST by block("test") { Test(BlockBehaviour.Properties.of()) }
 * }
 * ```
 */
open class BlockRegistryHelper<T : Block>(modId: String) : RegistryHelper<T>(
	DeferredRegister.create(modId, Registries.BLOCK) as DeferredRegister<T>
) {
	val itemRegistry = DeferredRegister.create(modId, Registries.ITEM)

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