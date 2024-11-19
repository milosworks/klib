package vyrek.kodek.networking

import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import vyrek.kodek.TestMod
import vyrek.kodek.networking.TPackets.Uwu

interface Payload: CustomPacketPayload {
	override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
		return CustomPacketPayload.Type(TestMod.id("test"))
	}
}

object TPackets {
	@Serializable
	data class Uwu(val str: String): Payload
}

object TKodek {
	val uwu: StreamCodec<RegistryFriendlyByteBuf, Uwu> = StreamCodec.of(
		{ buf, obj -> Cbor.encodeToByteArray(Uwu.serializer(), obj)}
	) { buf -> Cbor.decodeFromByteArray(Uwu.serializer(), buf.readByteArray()) }
}