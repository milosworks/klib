package vyrek.kodek.init

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import vyrek.kodek.TestMod
import vyrek.kodek.TestMod.LOGGER
import vyrek.kodek.networking.TKodek
import vyrek.kodek.networking.TPackets
import vyrek.kodek.networking.TPackets.Uwu

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
		val register = event.registrar("1")

		register.playBidirectional(
			CustomPacketPayload.Type(TestMod.id("test")),
			TKodek.uwu,
			DirectionalPayloadHandler(
				{uwu, context -> println(uwu.str)},
				{uwu, context -> println(uwu.str)}
			)
		)
	}
}

