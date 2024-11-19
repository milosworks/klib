package vyrek.kodek.init

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import vyrek.kodek.TestMod

object TItems {
	val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(TestMod.ID)

	fun init(event: IEventBus) {
		ITEMS.register(event)
	}
}