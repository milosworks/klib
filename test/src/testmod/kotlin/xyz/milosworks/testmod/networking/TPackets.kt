package xyz.milosworks.testmod.networking

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import xyz.milosworks.klib.networking.NetworkChannel
import xyz.milosworks.klib.serialization.CodecSerializer
import xyz.milosworks.klib.serialization.SerializationManager
import xyz.milosworks.klib.serialization.serializers.SBlockPos
import xyz.milosworks.klib.serialization.serializers.SFriendlyByteBuf
import xyz.milosworks.klib.serialization.serializers.SResourceLocation
import xyz.milosworks.testmod.TestMod

val CHANNEL = NetworkChannel(TestMod.id("main"))

val ComponentSerializer = CodecSerializer<Component>(ComponentSerialization.CODEC)

object TPackets {
	@Serializable
	data class Uwu(
		val str: String,
		val num: Int,
		val buf: SFriendlyByteBuf,
		val loc: SResourceLocation,
		@Contextual
		val com: Component,
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