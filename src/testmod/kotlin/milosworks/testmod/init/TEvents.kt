package milosworks.testmod.init

import milosworks.testmod.TestMod
import milosworks.testmod.TestMod.LOGGER
import milosworks.testmod.networking.CHANNEL
import milosworks.testmod.networking.TPackets
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

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

