package xyz.milosworks.klib.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.resources.ResourceLocation
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
 * @property registry The registry to where the entries will be registered to
 */
open class RegistryHelper<T : Any>(val registry: DeferredRegister<T>) {
	/**
	 * Registers this helper, this needs to be called in the init method of your mod.
	 */
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