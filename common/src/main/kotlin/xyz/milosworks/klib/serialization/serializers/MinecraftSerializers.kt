package xyz.milosworks.klib.serialization.serializers

import io.netty.buffer.Unpooled
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.core.*
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

//object BlockPos : KSerializer<MBlockPos> {
//	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockPos") {
//		element("x", PrimitiveSerialDescriptor("x", PrimitiveKind.INT))
//		element("y", PrimitiveSerialDescriptor("x", PrimitiveKind.INT))
//		element("z", PrimitiveSerialDescriptor("x", PrimitiveKind.INT))
//	}
//
//	override fun serialize(encoder: Encoder, value: MBlockPos) {
//		encoder.encodeStructure(descriptor) {
//			encodeIntElement(descriptor, 0, value.x)
//			encodeIntElement(descriptor, 1, value.y)
//			encodeIntElement(descriptor, 2, value.z)
//		}
//	}
//
//	override fun deserialize(decoder: Decoder): MBlockPos {
//		return decoder.decodeStructure(descriptor) {
//			var x = 0
//			var y = 0
//			var z = 0
//			while (true) {
//				when (val index = decodeElementIndex(descriptor)) {
//					0 -> x = decodeIntElement(descriptor, 0)
//					1 -> y = decodeIntElement(descriptor, 1)
//					2 -> z = decodeIntElement(descriptor, 2)
//					CompositeDecoder.DECODE_DONE -> break
//					else -> throw SerializationException("Unexpected index: $index")
//				}
//			}
//			MBlockPos(x, y, z)
//		}
//	}
//}

/* ------------------ TypeAliases ------------------ */

typealias SFriendlyByteBuf = @Contextual FriendlyByteBuf

typealias SResourceLocation = @Contextual ResourceLocation

typealias SVec3i = @Contextual Vec3i
typealias SVec3 = @Contextual Vec3

typealias SBlockPos = @Contextual BlockPos
typealias SChunkPos = @Contextual ChunkPos
typealias SGlobalPos = @Contextual GlobalPos

typealias SBlockHitResult = @Contextual BlockHitResult

/* ------------------ Serializers ------------------ */

object FriendlyByteBufSerializer : KSerializer<FriendlyByteBuf> {
	override val descriptor =
		SerialDescriptor("FriendlyByteBuf", ByteArraySerializer().descriptor)

	override fun serialize(encoder: Encoder, value: FriendlyByteBuf) {
		val index = value.readerIndex()
		val bytes = ByteArray(value.readableBytes())
		value.readBytes(bytes)
		value.readerIndex(index)

		encoder.encodeSerializableValue(ByteArraySerializer(), bytes)
	}

	override fun deserialize(decoder: Decoder): FriendlyByteBuf {
		val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
		val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
			writeBytes(bytes)
		}

		return buf
	}
}

object ResourceLocationSerializer : KSerializer<ResourceLocation> {
	override val descriptor = PrimitiveSerialDescriptor("ResourceLocation", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: ResourceLocation) = encoder.encodeString(value.toString())
	override fun deserialize(decoder: Decoder): ResourceLocation = ResourceLocation.parse(decoder.decodeString())
}

/**
 * A serializer for [ResourceKey] classes.
 *
 * The implementation mimics the codec creation behavior of [ResourceKey.codec], requiring access to the default registry.
 *
 * ### Example
 * ```kotlin
 * val DIMENSION = ResourceKeySerializer(Registries.DIMENSION)
 * ```
 *
 * @property registry The registry associated with the resource key.
 */
class ResourceKeySerializer<T : Any>(val registry: ResourceKey<out Registry<T>>) : KSerializer<ResourceKey<*>> {
	companion object {
		val DIMENSION = ResourceKeySerializer(Registries.DIMENSION)
	}

	override val descriptor: SerialDescriptor = ResourceLocationSerializer.descriptor
	override fun serialize(encoder: Encoder, value: ResourceKey<*>) =
		encoder.encodeSerializableValue(ResourceLocationSerializer, value.location())

	override fun deserialize(decoder: Decoder): ResourceKey<*> =
		ResourceKey.create<T>(registry, decoder.decodeSerializableValue(ResourceLocationSerializer))
}

object GlobalPosSerializer : KSerializer<GlobalPos> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GlobalPos") {
		element("dimension", ResourceKeySerializer.DIMENSION.descriptor)
		element("pos", BlockPosSerializer.descriptor)
	}

	override fun serialize(encoder: Encoder, value: GlobalPos) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, ResourceKeySerializer.DIMENSION, value.dimension)
			encodeSerializableElement(descriptor, 1, BlockPosSerializer, value.pos)
		}
	}


	override fun deserialize(decoder: Decoder): GlobalPos {
		return decoder.decodeStructure(descriptor) {
			var dimension: ResourceKey<Level>? = null
			var pos: BlockPos? = null

			while (true) {
				@Suppress("UNCHECKED_CAST")
				when (val index = decodeElementIndex(descriptor)) {
					0 -> dimension =
						decodeSerializableElement(descriptor, 0, ResourceKeySerializer.DIMENSION) as ResourceKey<Level>

					1 -> pos = decodeSerializableElement(descriptor, 1, BlockPosSerializer)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			if (dimension == null || pos == null) throw SerializationException("Error when decoding GlobalPos, incomplete data")

			GlobalPos(dimension, pos)
		}
	}
}

object Vec3iSerializer : KSerializer<Vec3i> {
	override val descriptor = buildClassSerialDescriptor("Vec3i") {
		element<Int>("x")
		element<Int>("y")
		element<Int>("z")
	}

	override fun serialize(encoder: Encoder, value: Vec3i) {
		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.x)
			encodeIntElement(descriptor, 1, value.y)
			encodeIntElement(descriptor, 2, value.z)
		}
	}

	override fun deserialize(decoder: Decoder): Vec3i {
		return decoder.decodeStructure(descriptor) {
			var x: Int? = null
			var y: Int? = null
			var z: Int? = null

			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> x = decodeIntElement(descriptor, 0)
					1 -> y = decodeIntElement(descriptor, 1)
					2 -> z = decodeIntElement(descriptor, 2)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			if (x == null || y == null || z == null)
				throw SerializationException("Error when decoding Vec3i, incomplete data")

			Vec3i(x, y, z)
		}
	}
}

object Vec3Serializer : KSerializer<Vec3> {
	override val descriptor = buildClassSerialDescriptor("Vec3") {
		element<Double>("x")
		element<Double>("y")
		element<Double>("z")
	}

	override fun serialize(encoder: Encoder, value: Vec3) {
		encoder.encodeStructure(descriptor) {
			encodeDoubleElement(descriptor, 0, value.x)
			encodeDoubleElement(descriptor, 1, value.y)
			encodeDoubleElement(descriptor, 2, value.z)
		}
	}

	override fun deserialize(decoder: Decoder): Vec3 {
		return decoder.decodeStructure(descriptor) {
			var x: Double? = null
			var y: Double? = null
			var z: Double? = null

			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> x = decodeDoubleElement(descriptor, 0)
					1 -> y = decodeDoubleElement(descriptor, 1)
					2 -> z = decodeDoubleElement(descriptor, 2)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			if (x == null || y == null || z == null)
				throw SerializationException("Error when decoding Vec3, incomplete data")

			Vec3(x, y, z)
		}
	}
}

object BlockPosSerializer : KSerializer<BlockPos> {
	override val descriptor = buildClassSerialDescriptor("BlockPos") {
		element<Int>("x")
		element<Int>("y")
		element<Int>("z")
	}

	override fun serialize(encoder: Encoder, value: BlockPos) {
		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.x)
			encodeIntElement(descriptor, 1, value.y)
			encodeIntElement(descriptor, 2, value.z)
		}
	}

	override fun deserialize(decoder: Decoder): BlockPos {
		return decoder.decodeStructure(descriptor) {
			var x: Int? = null
			var y: Int? = null
			var z: Int? = null

			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> x = decodeIntElement(descriptor, 0)
					1 -> y = decodeIntElement(descriptor, 1)
					2 -> z = decodeIntElement(descriptor, 2)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			if (x == null || y == null || z == null)
				throw SerializationException("Error when decoding BlockPos, incomplete data")

			BlockPos(x, y, z)
		}
	}
}

object ChunkPosSerializer : KSerializer<ChunkPos> {
	override val descriptor = PrimitiveSerialDescriptor("ChunkPos", PrimitiveKind.LONG)
	override fun serialize(encoder: Encoder, value: ChunkPos) {
		encoder.encodeLong(value.toLong())
	}

	override fun deserialize(decoder: Decoder): ChunkPos {
		return ChunkPos(decoder.decodeLong())
	}
}

object BlockHitResultSerializer : KSerializer<BlockHitResult> {
	override val descriptor = buildClassSerialDescriptor("BlockHitResult") {
		element("location", Vec3Serializer.descriptor)
		element<String>("side")
		element("blockPos", BlockPosSerializer.descriptor)
		element<Boolean>("insideBlock")
		element<Boolean>("missed")
	}

	override fun serialize(encoder: Encoder, value: BlockHitResult) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, Vec3Serializer, value.location)
			encodeStringElement(descriptor, 1, value.direction.name)
			encodeSerializableElement(descriptor, 2, BlockPosSerializer, value.blockPos)
			encodeBooleanElement(descriptor, 3, value.isInside)
			encodeBooleanElement(descriptor, 4, value.type == HitResult.Type.MISS)
		}
	}

	override fun deserialize(decoder: Decoder): BlockHitResult {
		var location: Vec3? = null
		var side: Direction? = null
		var pos: BlockPos? = null
		var inside: Boolean? = null
		var missed: Boolean? = null

		decoder.decodeStructure(descriptor) {
			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> location = decodeSerializableElement(descriptor, 0, Vec3Serializer)
					1 -> side = Direction.byName(decodeStringElement(descriptor, 1))
					2 -> pos = decodeSerializableElement(descriptor, 2, BlockPosSerializer)
					3 -> inside = decodeBooleanElement(descriptor, 3)
					4 -> missed = decodeBooleanElement(descriptor, 4)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}
		}

		if (location == null || side == null || pos == null || inside == null || missed == null) throw SerializationException(
			"Properties missing when decoding"
		)

		return if (missed == true) BlockHitResult.miss(location, side!!, pos) else BlockHitResult(
			location,
			side!!,
			pos,
			inside
		)
	}
}

internal val MinecraftSerializersModule = SerializersModule {
	contextual(FriendlyByteBuf::class, FriendlyByteBufSerializer)

	contextual(ResourceLocation::class, ResourceLocationSerializer)

	contextual(Vec3i::class, Vec3iSerializer)
	contextual(Vec3::class, Vec3Serializer)

	contextual(BlockPos::class, BlockPosSerializer)
	contextual(ChunkPos::class, ChunkPosSerializer)
	contextual(GlobalPos::class, GlobalPosSerializer)

	contextual(BlockHitResult::class, BlockHitResultSerializer)
}