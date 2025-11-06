package xyz.milosworks.klib.registry

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab

/**
 * Helper class for CreativeTab registration
 * ### Example
 * ```kotlin
 * object TCreativeTabs : CreativeTabRegistryHelper<CreativeModeTab>(TestMod.ID) {
 * 	val TAB by create("tab") { ->
 * 		title(Component.literal("Test"))
 * 		icon { ItemStack(TBlocks.TEST) }
 * 		displayItems { _, o ->
 * 			o.accept(TBlocks.TEST)
 * 		}
 * 	}
 * }
 *```
 * @param modId The ID of your mod
 */
@Suppress("UNCHECKED_CAST")
open class CreativeTabRegistryHelper<T : CreativeModeTab>(modId: String) : RegistryHelper<T>(
	DeferredRegister.create(modId, Registries.CREATIVE_MODE_TAB) as DeferredRegister<T>
) {
	open fun <V : T> create(id: String, block: CreativeModeTab.Builder.() -> Unit): RegistrySupplier<V> =
		register<V>(id) { -> CreativeTabRegistry.create(block) as V }
}