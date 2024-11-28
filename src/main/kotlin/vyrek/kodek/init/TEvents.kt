package vyrek.kodek.init

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import vyrek.kodek.TestMod
import vyrek.kodek.TestMod.LOGGER
import vyrek.kodek.networking.CHANNEL
import vyrek.kodek.networking.TPackets

@EventBusSubscriber(modid = TestMod.ID, bus = EventBusSubscriber.Bus.MOD)
object TEvents {
	@SubscribeEvent
	fun onCommonSetup(event: FMLCommonSetupEvent) {
		LOGGER.info("Mod test initiated up")
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	fun onClientSetup(event: FMLClientSetupEvent) {
		LOGGER.info("Mod test Client initiated up")
	}

	@SubscribeEvent
	fun onRegisterPayloadHandlers(event: RegisterPayloadHandlersEvent) {
		TPackets.init()
		CHANNEL.register(event)
	}
}

