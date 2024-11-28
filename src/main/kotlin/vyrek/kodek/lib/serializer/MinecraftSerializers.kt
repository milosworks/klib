package vyrek.kodek.lib.serializer

import io.netty.buffer.Unpooled
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.core.Direction
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos as MBlockPos
import net.minecraft.core.Vec3i as MVec3i
import net.minecraft.network.FriendlyByteBuf as MFriendlyByteBuf
import net.minecraft.resources.ResourceLocation as MResourceLocation
import net.minecraft.world.level.ChunkPos as MChunkPos
import net.minecraft.world.phys.BlockHitResult as MBlockHitResult
import net.minecraft.world.phys.Vec3 as MVec3

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

typealias SFriendlyByteBuf = @Contextual MFriendlyByteBuf

typealias SResourceLocation = @Contextual MResourceLocation

typealias SVec3i = @Contextual MVec3i
typealias SVec3 = @Contextual MVec3

typealias SBlockPos = @Contextual MBlockPos
typealias SChunkPos = @Contextual MChunkPos

typealias SBlockHitResult = @Contextual MBlockHitResult

/**
 * Some serializers for Minecraft's classes
 */
object MinecraftSerializers {
	object FriendlyByteBuf : KSerializer<MFriendlyByteBuf> {
		override val descriptor =
			SerialDescriptor("FriendlyByteBuf", ByteArraySerializer().descriptor)

		override fun serialize(encoder: Encoder, value: MFriendlyByteBuf) {
			val index = value.readerIndex()
			val bytes = ByteArray(value.readableBytes())
			value.readBytes(bytes)
			value.readerIndex(index)

			encoder.encodeSerializableValue(ByteArraySerializer(), bytes)
		}

		override fun deserialize(decoder: Decoder): MFriendlyByteBuf {
			val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
			val buf = MFriendlyByteBuf(Unpooled.buffer()).apply {
				writeBytes(bytes)
			}

			return buf
		}
	}

	object ResourceLocation : KSerializer<MResourceLocation> {
		override val descriptor = PrimitiveSerialDescriptor("ResourceLocation", PrimitiveKind.STRING)
		override fun serialize(encoder: Encoder, value: MResourceLocation) {
			encoder.encodeString(value.toString())
		}

		override fun deserialize(decoder: Decoder): MResourceLocation {
			return MResourceLocation.parse(decoder.decodeString())
		}
	}

	// TODO: Component codec
//	val SComponent = fromCodec(ComponentSerialization.CODEC)

	val Vec3i = VectorSerializer.create<Int, MVec3i>(
		{ x, y, z -> MVec3i(x, y, z) }, { x }, { y }, { z }
	)
	val Vec3 = VectorSerializer.create<Double, MVec3>(
		{ x, y, z -> MVec3(x, y, z) }, { x }, { y }, { z }
	)

	val BlockPos = VectorSerializer.create<Int, MBlockPos>(
		{ x, y, z -> MBlockPos(x, y, z) }, { x }, { y }, { z }
	)

	object ChunkPos : KSerializer<MChunkPos> {
		override val descriptor = PrimitiveSerialDescriptor("ChunkPos", PrimitiveKind.LONG)
		override fun serialize(encoder: Encoder, value: MChunkPos) {
			encoder.encodeLong(value.toLong())
		}

		override fun deserialize(decoder: Decoder): MChunkPos {
			return MChunkPos(decoder.decodeLong())
		}
	}

	object BlockHitResult : KSerializer<MBlockHitResult> {
		override val descriptor = buildClassSerialDescriptor("BlockHitResult") {
			element("location", Vec3.descriptor)
			element<String>("side")
			element("blockPos", BlockPos.descriptor)
			element<Boolean>("insideBlock")
			element<Boolean>("missed")
		}

		override fun serialize(encoder: Encoder, value: MBlockHitResult) {
			encoder.encodeStructure(descriptor) {
				encodeSerializableElement(descriptor, 0, Vec3, value.location)
				encodeStringElement(descriptor, 1, value.direction.name)
				encodeSerializableElement(descriptor, 2, BlockPos, value.blockPos)
				encodeBooleanElement(descriptor, 3, value.isInside)
				encodeBooleanElement(descriptor, 4, value.type == HitResult.Type.MISS)
			}
		}

		override fun deserialize(decoder: Decoder): MBlockHitResult {
			var location: MVec3? = null
			var side: Direction? = null
			var pos: MBlockPos? = null
			var inside: Boolean? = null
			var missed: Boolean? = null

			decoder.decodeStructure(descriptor) {
				while (true) {
					when (val index = decodeElementIndex(descriptor)) {
						0 -> location = decodeSerializableElement(descriptor, 0, Vec3)
						1 -> side = Direction.byName(decodeStringElement(descriptor, 1))
						2 -> pos = decodeSerializableElement(descriptor, 2, BlockPos)
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

			return if (missed == true) MBlockHitResult.miss(location, side!!, pos) else MBlockHitResult(
				location,
				side,
				pos,
				inside
			)
		}
	}

	/**
	 * @suppress suppress for dokka
	 */
	val module = SerializersModule {
		contextual(MFriendlyByteBuf::class, FriendlyByteBuf)

		contextual(MResourceLocation::class, ResourceLocation)

		contextual(MVec3i::class, Vec3i)
		contextual(MVec3::class, Vec3)

		contextual(MBlockPos::class, BlockPos)
		contextual(MChunkPos::class, ChunkPos)

		contextual(MBlockHitResult::class, BlockHitResult)
	}
}

/**
 * @suppress suppress for dokka
 */
class VectorSerializer<T, K : Any>(
	name: String,
	val constructor: (x: T, y: T, z: T) -> K,
	kind: PrimitiveKind,
	val primitiveSerializer: KSerializer<T>,
	val getX: K.() -> T,
	val getY: K.() -> T,
	val getZ: K.() -> T
) : KSerializer<K> {
	companion object {
		inline fun <reified T, reified K : Any> create(
			noinline constructor: (x: T, y: T, z: T) -> K,
			noinline getX: K.() -> T,
			noinline getY: K.() -> T,
			noinline getZ: K.() -> T
		): VectorSerializer<T, K> {
			val name = K::class.simpleName ?: throw SerializationException("No name found for vector class")
			val primitiveKind = when (T::class) {
				Int::class -> PrimitiveKind.INT
				Float::class -> PrimitiveKind.FLOAT
				Double::class -> PrimitiveKind.DOUBLE
				else -> throw SerializationException("Primitive type not supported in vector serializer")
			}

			@Suppress("UNCHECKED_CAST")
			val primitiveSerializer = when (T::class) {
				Int::class -> Int.serializer()
				Float::class -> Float.serializer()
				Double::class -> Double.serializer()
				else -> throw SerializationException("Primitive type not supported in vector serializer")
			} as KSerializer<T>

			return VectorSerializer<T, K>(name, constructor, primitiveKind, primitiveSerializer, getX, getY, getZ)
		}
	}

	override val descriptor: SerialDescriptor = buildClassSerialDescriptor(name) {
		element("x", PrimitiveSerialDescriptor("x", kind))
		element("y", PrimitiveSerialDescriptor("y", kind))
		element("z", PrimitiveSerialDescriptor("z", kind))
	}

	override fun serialize(encoder: Encoder, value: K) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, primitiveSerializer, value.getX())
			encodeSerializableElement(descriptor, 1, primitiveSerializer, value.getY())
			encodeSerializableElement(descriptor, 2, primitiveSerializer, value.getZ())
		}
	}

	override fun deserialize(decoder: Decoder): K {
		return decoder.decodeStructure(descriptor) {
			var x: T? = null
			var y: T? = null
			var z: T? = null

			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> x = decodeSerializableElement(descriptor, 0, primitiveSerializer)
					1 -> y = decodeSerializableElement(descriptor, 1, primitiveSerializer)
					2 -> z = decodeSerializableElement(descriptor, 2, primitiveSerializer)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			if (x != null && y != null && z != null) {
				constructor(x, y, z)
			} else {
				throw SerializationException("Failed to decode vector")
			}
		}
	}
}
