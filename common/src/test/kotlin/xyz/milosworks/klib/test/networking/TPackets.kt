package xyz.milosworks.klib.test.networking

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import xyz.milosworks.klib.serialization.SerializationManager
import xyz.milosworks.klib.serialization.serializers.SBlockPos
import xyz.milosworks.klib.serialization.serializers.SFriendlyByteBuf
import xyz.milosworks.klib.serialization.serializers.SResourceLocation
import xyz.milosworks.klib.test.init.TChannels.CHANNEL

object TPackets {
	@Serializable
	data class Uwu(
		val str: String,
		val num: Int,
		val buf: SFriendlyByteBuf,
		val loc: SResourceLocation,
		val com: SComponent,
		val pos: SBlockPos
	)

	fun init() {
		SerializationManager {
			module {
				contextual(Component::class, ComponentSerializer)
			}
		}

		CHANNEL.serverbound(Uwu::class) { msg, ctx ->
			println(msg)
		}

		CHANNEL.clientbound(Uwu::class) { msg, ctx ->
			println(msg)
		}
	}
}