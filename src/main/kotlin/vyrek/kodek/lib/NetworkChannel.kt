@file:OptIn(InternalSerializationApi::class)

package vyrek.kodek.lib

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler
import net.neoforged.neoforge.network.handling.IPayloadContext
import vyrek.kodek.lib.serializer.SResourceLocation
import vyrek.kodek.lib.serializer.SerializationManager
import vyrek.kodek.lib.serializer.toStreamCodec
import kotlin.reflect.KClass

/**
 * A function type representing a handler for processing user-defined packets.
 */
typealias PacketHandler <T> = (T, IPayloadContext) -> Unit

@Serializable
internal data class Payload(val id: SResourceLocation, val index: Int, val data: ByteArray) : CustomPacketPayload {
	override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
		return CustomPacketPayload.Type(id)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Payload

		if (index != other.index) return false
		if (!data.contentEquals(other.data)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = index
		result = 31 * result + data.contentHashCode()
		return result
	}
}

private val PayloadCodec = Payload.serializer().toStreamCodec()

class NetworkChannel(val id: ResourceLocation) {
	private val packetId = CustomPacketPayload.Type<Payload>(id)

	private val serverClasses = mutableListOf<KClass<*>>()
	private val clientClasses = mutableListOf<KClass<*>>()

	private val serverboundHandlers = mutableListOf<PacketHandler<*>>()
	private val clientboundHandlers = mutableListOf<PacketHandler<*>>()

	fun <T : Any> serverbound(klass: KClass<T>, handler: PacketHandler<T>) {
		require(klass.isData) { "Only data classes can be used as packets" }
		require(klass.serializerOrNull() != null) { "Data class doesn't have a serializer. Did you forget to add @Serializable?" }
		require(serverClasses.find { it == klass } == null) { "Packet is already registered" }

		serverboundHandlers.add(handler)
		serverClasses.add(klass)
	}

	fun <T : Any> clientbound(klass: KClass<T>, handler: PacketHandler<T>) {
		require(klass.isData) { "Only data classes can be used as packets." }
		require(klass.serializerOrNull() != null) { "Data class doesn't have a serializer. Did you forget to add @Serializable?" }
		require(clientClasses.find { it == klass } == null) { "Packet is already registered" }

		clientboundHandlers.add(handler)
		clientClasses.add(klass)
	}

	fun <T : Any> toServer(vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		packets.map {
			@Suppress("UNCHECKED_CAST")
			val klass = serverClasses.find { x -> x == it::class } as? KClass<T>
				?: throw IllegalStateException()
			val index = serverClasses.indexOf(klass)
			val bytes = SerializationManager.cbor.encodeToByteArray(klass.serializer(), it)

			Payload(id, index, bytes)
		}.also {
			PacketDistributor.sendToServer(it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toPlayer(player: ServerPlayer, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to client but client hasn't registered the packet and its handler",
			packets
		).also {
			PacketDistributor.sendToPlayer(player, it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toAllPlayers(vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to client but client hasn't registered the packet and its handler",
			packets
		).also {
			PacketDistributor.sendToAllPlayers(it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toPlayersInDimension(level: ServerLevel, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to client but client hasn't registered the packet and its handler",
			packets
		).also {
			PacketDistributor.sendToPlayersInDimension(level, it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toNearPlayers(
		level: ServerLevel,
		exclude: ServerPlayer? = null,
		x: Double,
		y: Double,
		z: Double,
		radius: Double,
		vararg packets: T
	) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to client but client hasn't registered the packet and its handler",
			packets
		).also {
			PacketDistributor.sendToPlayersNear(level, exclude, x, y, z, radius, it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toPlayersTrackingEntity(entity: Entity, self: Boolean = false, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to clients but client hasn't registered the packet and its handler",
			packets
		).also {
			if (self) PacketDistributor.sendToPlayersTrackingEntityAndSelf(
				entity,
				it.first(),
				*it.drop(1).toTypedArray()
			)
			else PacketDistributor.sendToPlayersTrackingEntity(entity, it.first(), *it.drop(1).toTypedArray())
		}
	}

	fun <T : Any> toPlayersTrackingChunk(level: ServerLevel, pos: ChunkPos, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(
			"Trying to send a packet to clients but client hasn't registered the packet and its handler",
			packets
		).also {
			PacketDistributor.sendToPlayersTrackingChunk(level, pos, it.first(), *it.drop(1).toTypedArray())
		}
	}

	private fun <T : Any> createPayloads(message: String, vararg packets: T): List<Payload> {
		return packets.map {
			@Suppress("UNCHECKED_CAST")
			val klass = clientClasses.find { x -> x == it::class } as? KClass<T>
				?: throw IllegalStateException(message)
			val index = clientClasses.indexOf(klass)
			val bytes = SerializationManager.cbor.encodeToByteArray(klass.serializer(), it)

			Payload(id, index, bytes)
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun register(event: RegisterPayloadHandlersEvent) {
		val registry = event.registrar(id.toString())

		registry.playBidirectional(
			packetId,
			PayloadCodec,
			DirectionalPayloadHandler(
				{ payload, ctx ->
					val klass = clientClasses.getOrNull(payload.index)
						?: throw NoSuchElementException("No class was found on the clientside. Did you forget to do clientbound?")

					val handler = clientboundHandlers.getOrNull(payload.index) as? PacketHandler<Any>
						?: throw NoSuchElementException("No handler was found on the clientside. Did you forget to do clientbound?")

					val msg = SerializationManager.cbor.decodeFromByteArray(klass.serializer(), payload.data)

					handler(msg, ctx)
				},
				{ payload, ctx ->
					val klass = serverClasses.getOrNull(payload.index)
						?: throw NoSuchElementException("No class was found on the serverside. Did you forget to do serverbound?")

					val handler = serverboundHandlers.getOrNull(payload.index) as? PacketHandler<Any>
						?: throw NoSuchElementException("No handler was found on the serverside. Did you forget to do serverbound?")

					val msg = SerializationManager.cbor.decodeFromByteArray(klass.serializer(), payload.data)

					handler(msg, ctx)
				}
			)
		)
	}
}