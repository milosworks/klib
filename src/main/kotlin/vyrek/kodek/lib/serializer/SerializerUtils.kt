package vyrek.kodek.lib.serializer

import com.google.gson.JsonParser
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import net.benwoodworth.knbt.Nbt
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import com.google.gson.JsonElement as GsonElement

/**
 * EXPERIMENTAL: Convert a minecraft codec into a KSerializer,
 * its HIGHLY recommended to manually make a KSerializer for the codec or
 * use any of the already created KSerializers.
 */
inline fun <reified T : Any> Codec<T>.toKSerializer(): KSerializer<T> {
	val name = T::class.simpleName ?: throw SerializationException("No name found for the codec class")

	return object : KSerializer<T> {
		override val descriptor: SerialDescriptor = buildClassSerialDescriptor(name)

		override fun serialize(encoder: Encoder, value: T) {
			val byteArrayOutputStream = ByteArrayOutputStream()
			val dataOutputStream = DataOutputStream(byteArrayOutputStream)

			val tag = this@toKSerializer.encodeStart(NbtOps.INSTANCE, value)
				.getOrThrow { t -> throw SerializationException("Serialization failed: $t") }

			tag.write(dataOutputStream)

			dataOutputStream.close()

			val bytes = byteArrayOutputStream.toByteArray()

			encoder.encodeSerializableValue(ByteArraySerializer(), bytes)
		}

		override fun deserialize(decoder: Decoder): T {
			val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
			val byteArrayInputStream = ByteArrayInputStream(bytes)
			val dataInputStream = DataInputStream(byteArrayInputStream)

			val tag = NbtIo.readAnyTag(dataInputStream, NbtAccounter.unlimitedHeap())
			dataInputStream.close()

			val value = this@toKSerializer.decode(NbtOps.INSTANCE, tag)
				.getOrThrow { err -> throw SerializationException("Deserialization failed: $err") }

			return value.first as T
		}
	}
}

//fun <T : Any> Codec<T>.toKSerializer(serialDescriptor: SerialDescriptor? = null): KSerializer<T> {
//	return object : KSerializer<T> {
//		override val descriptor: SerialDescriptor
//			get() {
//				if (serialDescriptor != null) return serialDescriptor
//
//
//			}
//	}
//}

/**
 * Converts a [KSerializer] into a [Codec]. This function enables serialization and deserialization
 * between Minecraft's `DynamicOps` ([JsonOps] and [NbtOps]) and Kotlin objects using kotlinx.serialization.
 *
 * Example usage:
 *
 * ```kotlin
 * @Serializable
 * data class TestData(
 *     val str: String,
 *     val int: Int,
 *     val float: Float,
 *     val double: Double,
 *     val boolean: Boolean
 * )
 *
 * // Using the `toCodec` extension:
 * val TestCodec = TestData.serializer().toCodec()
 *
 * // The above is equivalent to manually creating a codec:
 * val ManualTestCodec: Codec<TestData> = RecordCodecBuilder.create {
 *     it.group(
 *         Codec.STRING.fieldOf("str").forGetter(TestData::str),
 *         Codec.INT.fieldOf("int").forGetter(TestData::int),
 *         Codec.FLOAT.fieldOf("float").forGetter(TestData::float),
 *         Codec.DOUBLE.fieldOf("double").forGetter(TestData::double),
 *         Codec.BOOL.fieldOf("boolean").forGetter(TestData::boolean)
 *     ).apply(it, ::TestData)
 * }
 * ```
 *
 * **Note:** Only `JsonOps` and `NbtOps` are supported. Attempting to use other `DynamicOps` implementations
 * will result in an [UnsupportedOperationException].
 *
 * @return A [Codec] capable of encoding and decoding the type `T` defined by the [KSerializer].
 * @throws UnsupportedOperationException If the provided `DynamicOps` type is not supported.
 */
fun <T : Any> KSerializer<T>.toCodec(): Codec<T> {
	@Suppress("UNCHECKED_CAST")
	return object : Codec<T> {
		override fun <V : Any> encode(input: T, ops: DynamicOps<V>, prefix: V): DataResult<V> {
			return tryOrThrow<V> {
				val serializer = SerializationManager[ops]
					?: throw UnsupportedOperationException("${ops::class.simpleName} is not a supported DynamicOps instance.")

				when (serializer) {
					is Json -> serializer.encodeToJsonElement(this@toCodec, input).toGson
					is Nbt -> serializer.encodeToNbtTag(this@toCodec, input).toMinecraftTag
					else -> throw UnsupportedOperationException("DynamicOps of type ${ops::class.simpleName} is not supported for encoding.")
				} as V
			}
		}

		override fun <V : Any> decode(
			ops: DynamicOps<V>,
			input: V
		): DataResult<Pair<T, V>> {
			return tryOrThrow<Pair<T, V>> {
				val serializer = SerializationManager[ops]
					?: throw UnsupportedOperationException("${ops::class.simpleName} is not a supported DynamicOps instance.")

				val value = when (serializer) {
					is Json -> {
						if (input !is JsonElement)
							throw IllegalArgumentException("Expected input of type JsonElement but received ${input?.javaClass?.simpleName}.")

						serializer.decodeFromJsonElement(this@toCodec, input)
					}

					is Nbt -> {
						if (input !is Tag)
							throw IllegalArgumentException("Expected input of type Tag but received ${input?.javaClass?.simpleName}.")

						serializer.decodeFromNbtTag(
							this@toCodec,
							input.toKNbt
								?: throw IllegalStateException("Failed to convert a Minecraft Tag into a KNbtTag.")
						)
					}

					else -> UnsupportedOperationException("DynamicOps of type ${ops::class.simpleName} is not supported for decoding.")
				} as T

				Pair(value, input)
			}
		}
	}
}

internal fun <T : Any> tryOrThrow(action: () -> T): DataResult<T> {
	return try {
		DataResult.success(action())
	} catch (err: Exception) {
		DataResult.error(err::message)
	}
}

val JsonElement.toGson: GsonElement
	get() = JsonParser.parseString(this.toString())

/**
 * Converts any KSerializer into a StreamCodec using Cbor
 */
fun <T : Any> KSerializer<T>.toStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, T> {
	return StreamCodec.of<RegistryFriendlyByteBuf, T>(
		{ buffer, value -> buffer.writeByteArray(SerializationManager.cbor.encodeToByteArray(serializer(), value)) },
		{ buffer -> SerializationManager.cbor.decodeFromByteArray(serializer(), buffer.readByteArray()) }
	)
}

