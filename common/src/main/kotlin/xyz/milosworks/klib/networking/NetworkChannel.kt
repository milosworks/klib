@file:OptIn(InternalSerializationApi::class)

package xyz.milosworks.klib.networking

import dev.architectury.networking.NetworkManager
import dev.architectury.utils.GameInstance
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import net.minecraft.core.RegistryAccess
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerChunkCache
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ChunkPos
import xyz.milosworks.klib.serialization.SerializationManager
import xyz.milosworks.klib.serialization.serializers.SResourceLocation
import xyz.milosworks.klib.serialization.streamCodec
import kotlin.reflect.KClass

/**
 * A function type representing a handler for processing user-defined packets.
 */
typealias PacketHandler <T> = (T, IPacketContext) -> Unit

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

internal val PayloadCodec = Payload.serializer().streamCodec

/**
 * Manages network packets by registering and handling packets sent between the client and server.
 * You could use only one channel for every packet.
 *
 * @param id The unique identifier for this network channel.
 */
class NetworkChannel(private val id: ResourceLocation) {
	private val packetId = CustomPacketPayload.Type<Payload>(id)

	private val serverClasses = mutableListOf<KClass<*>>()
	private val clientClasses = mutableListOf<KClass<*>>()

	private val serverboundHandlers = mutableListOf<PacketHandler<*>>()
	private val clientboundHandlers = mutableListOf<PacketHandler<*>>()

	/**
	 * Registers a server-bound packet and its handler.
	 *
	 * @param klass The class of the packet.
	 * @param handler The function to handle the packet on the server.
	 * @throws IllegalArgumentException if the packet is not a data class or lacks a `KSerializer`.
	 */
	fun <T : Any> serverbound(klass: KClass<T>, handler: PacketHandler<T>) {
		require(klass.isData) { "Only data classes can be used as packets" }
		require(klass.serializerOrNull() != null) { "Data class doesn't have a serializer. Did you forget to add @Serializable?" }
		require(serverClasses.find { it == klass } == null) { "Packet is already registered" }

		serverboundHandlers.add(handler)
		serverClasses.add(klass)
	}

	/**
	 * Registers a client-bound packet and its handler.
	 *
	 * @param klass The class of the packet.
	 * @param handler The function to handle the packet on the client.
	 * @throws IllegalArgumentException if the packet is not a data class or lacks a `KSerializer`.
	 */
	fun <T : Any> clientbound(klass: KClass<T>, handler: PacketHandler<T>) {
		require(klass.isData) { "Only data classes can be used as packets." }
		require(klass.serializerOrNull() != null) { "Data class doesn't have a serializer. Did you forget to add @Serializable?" }
		require(clientClasses.find { it == klass } == null) { "Packet is already registered" }

		clientboundHandlers.add(handler)
		clientClasses.add(klass)
	}

	/**
	 * Sends packets to the server.
	 *
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toServer(vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		packets.map {
			@Suppress("UNCHECKED_CAST")
			val klass = serverClasses.find { x -> x == it::class } as? KClass<T>
				?: throw IllegalStateException("Trying to send a packet to server but it hasn't registered the packet and its handler")
			val index = serverClasses.indexOf(klass)
			val bytes = SerializationManager.cbor.encodeToByteArray(klass.serializer(), it)

			Payload(id, index, bytes)
		}.forEach {
			NetworkManager.sendToServer(it)
		}
	}

	/**
	 * Sends packets to a specific player.
	 *
	 * @param player The player to send the packets to.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toPlayer(player: ServerPlayer, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(packets).forEach { NetworkManager.sendToPlayer(player, it) }
	}

	/**
	 * Sends packets to all players in the list.
	 *
	 * @param players The list of players to send the packet to.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toPlayers(players: List<ServerPlayer>, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(packets).forEach { NetworkManager.sendToPlayers(players, it) }
	}

	/**
	 * Sends packets to all players.
	 *
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toAllPlayers(vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }
		val server =
			GameInstance.getServer() ?: throw IllegalStateException("Cannot send clientbound payloads on the client")

		createPayloads(packets).forEach { NetworkManager.sendToPlayers(server.playerList.players, it) }
	}

	/**
	 * Sends packets to players in a specific dimension.
	 *
	 * @param level The server level representing the dimension.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toPlayersInDimension(level: ServerLevel, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(packets).forEach { NetworkManager.sendToPlayers(level.players(), it) }
	}

	/**
	 * Sends packets to players near a specific location.
	 *
	 * @param level The server level where players are located.
	 * @param exclude A player to exclude from receiving the packet, or `null`.
	 * @param x The X-coordinate of the location.
	 * @param y The Y-coordinate of the location.
	 * @param z The Z-coordinate of the location.
	 * @param radius The radius within which players will receive the packet.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
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

		createPayloads(packets).also {
			level.server.playerList.broadcast(
				exclude,
				x,
				y,
				z,
				radius,
				level.dimension(),
				makeClientboundPacket(it as CustomPacketPayload)
			)
		}

	}

	/**
	 * Sends packets to players tracking a specific entity.
	 * A player is tracking an entity if its loaded on the client.
	 *
	 * @param entity The entity being tracked.
	 * @param self Whether to include the player when sending the packets.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toPlayersTrackingEntity(entity: Entity, self: Boolean = false, vararg packets: T) {
		require(packets.isNotEmpty()) { "You need to specify one or more packets to send" }

		createPayloads(packets).also {
			val chunk = entity.level().chunkSource as? ServerChunkCache
				?: throw IllegalStateException("Cannot send clientbound payloads on the client")

			if (self) chunk.broadcastAndSend(entity, makeClientboundPacket(it as CustomPacketPayload))
			else chunk.broadcast(entity, makeClientboundPacket(it as CustomPacketPayload))
		}
	}

	/**
	 * Sends packets to players tracking a specific chunk.
	 * A player is tracking a chunk if its loaded on the client.
	 *
	 * @param level The server level containing the chunk.
	 * @param pos The position of the chunk.
	 * @param packets The packets to send.
	 * @throws IllegalArgumentException if no packets are provided.
	 */
	fun <T : Any> toPlayersTrackingChunk(level: ServerLevel, pos: ChunkPos, vararg packets: T) =
		toPlayers(level.chunkSource.chunkMap.getPlayers(pos, false), packets)

	private fun <T : Any> createPayloads(vararg packets: T): List<Payload> {
		return packets.map {
			@Suppress("UNCHECKED_CAST")
			val klass = clientClasses.find { x -> x == it::class } as? KClass<T>
				?: throw IllegalStateException("Trying to send a packet to clients but client hasn't registered the packet and its handler")
			val index = clientClasses.indexOf(klass)
			val bytes = SerializationManager.cbor.encodeToByteArray(klass.serializer(), it)

			Payload(id, index, bytes)
		}
	}

	private fun makeClientboundPacket(vararg payloads: CustomPacketPayload): Packet<*> {
		return if (payloads.size == 1) ClientboundCustomPayloadPacket(payloads.first())
		else ClientboundBundlePacket(payloads.map { ClientboundCustomPayloadPacket(it) })
	}

	/**
	 * Registers this network channel.
	 * This needs to be called during mod initialization.
	 */
	@Suppress("UNCHECKED_CAST")
	fun register() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, packetId, PayloadCodec) { payload, ctx ->
			val klass = clientClasses.getOrNull(payload.index)
				?: throw NoSuchElementException("No class was found on the clientside. Did you forget to do clientbound?")

			val handler = clientboundHandlers.getOrNull(payload.index) as? PacketHandler<Any>
				?: throw NoSuchElementException("No handler was found on the clientside. Did you forget to do clientbound?")

			val msg = SerializationManager.cbor.decodeFromByteArray(klass.serializer(), payload.data)

			handler(msg, object : IPacketContext {
				override val player: Player
					get() = ctx.player
				override val registryAccess: RegistryAccess
					get() = ctx.registryAccess()
			})
		}

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, packetId, PayloadCodec) { payload, ctx ->
			val klass = serverClasses.getOrNull(payload.index)
				?: throw NoSuchElementException("No class was found on the serverside. Did you forget to do serverbound?")

			val handler = serverboundHandlers.getOrNull(payload.index) as? PacketHandler<Any>
				?: throw NoSuchElementException("No handler was found on the serverside. Did you forget to do serverbound?")

			val msg = SerializationManager.cbor.decodeFromByteArray(klass.serializer(), payload.data)

			handler(msg, object : IPacketContext {
				override val player: Player
					get() = ctx.player
				override val registryAccess: RegistryAccess
					get() = ctx.registryAccess()
			})
		}
	}
}