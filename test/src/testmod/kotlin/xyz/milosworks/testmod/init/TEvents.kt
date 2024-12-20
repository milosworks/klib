package xyz.milosworks.testmod.init

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import xyz.milosworks.testmod.TestMod
import xyz.milosworks.testmod.TestMod.LOGGER
import xyz.milosworks.testmod.networking.CHANNEL
import xyz.milosworks.testmod.networking.TPackets

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

