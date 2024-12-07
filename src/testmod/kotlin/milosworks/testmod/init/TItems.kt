package milosworks.testmod.init

import milosworks.testmod.TestMod
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister

object TItems {
	val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(TestMod.ID)

	fun init(event: IEventBus) {
		ITEMS.register(event)
	}
}