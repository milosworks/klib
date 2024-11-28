package vyrek.kodek.networking

import kotlinx.serialization.Serializable
import vyrek.kodek.TestMod
import vyrek.kodek.lib.NetworkChannel
import vyrek.kodek.lib.serializer.SBlockPos
import vyrek.kodek.lib.serializer.SFriendlyByteBuf
import vyrek.kodek.lib.serializer.SResourceLocation

val CHANNEL = NetworkChannel(TestMod.id("main"))

object TPackets {
	@Serializable
	data class Uwu(
		val str: String,
		val num: Int,
		val buf: SFriendlyByteBuf,
		val loc: SResourceLocation,
//		@Contextual
//		val com: Component,
		val pos: SBlockPos
	)

	fun init() {
		CHANNEL.serverbound(Uwu::class) { msg, ctx ->
			println(msg)
		}

		CHANNEL.clientbound(Uwu::class) { msg, ctx ->
			println(msg)
		}
	}
}