package xyz.milosworks.klib.serialization

import com.google.gson.JsonParser
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import com.google.gson.JsonElement as GsonElement

/**
 * Converts a [Codec] into a [KSerializer].
 *
 * **Note:** By default `JsonOps` and `NbtOps` are supported. Check [SerializationManager.registerOp] to register another [DynamicOps].
 * Trying to use unregistered [DynamicOps] implementations will result in an [UnsupportedOperationException].
 */
val <T : Any> Codec<T>.kSerializer: KSerializer<T>
	get() = CodecSerializer(this)

/**
 * Converts a [KSerializer] into a [Codec].
 *
 * **Note:** By default `JsonOps` and `NbtOps` are supported. Check [SerializationManager.registerOp] to register another [DynamicOps].
 * Trying to use unregistered [DynamicOps] implementations will result in an [UnsupportedOperationException].
 *
 * ### Example
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
 * // Using the toCodec extension:
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
 * @return A [Codec] of type `T` defined by the [KSerializer].
 * @throws UnsupportedOperationException If the provided `DynamicOps` type is not supported.
 */
val <T : Any> KSerializer<T>.codec: Codec<T>
	get() = SerializerCodec(this)

/**
 * Converts any [KSerializer] into a [StreamCodec] using Cbor
 */
val <T : Any> KSerializer<T>.streamCodec: StreamCodec<RegistryFriendlyByteBuf, T>
	get() = StreamCodec.of<RegistryFriendlyByteBuf, T>(
		{ buffer, value -> buffer.writeByteArray(SerializationManager.cbor.encodeToByteArray(this, value)) },
		{ buffer -> SerializationManager.cbor.decodeFromByteArray(this, buffer.readByteArray()) }
	)

/**
 * Returns serial descriptor that delegates all the calls to descriptor returned by [deferred] block.
 * Used to resolve cyclic dependencies between recursive serializable structures.
 */
fun defer(deferred: () -> SerialDescriptor): SerialDescriptor = object : SerialDescriptor {

	private val original: SerialDescriptor by lazy(deferred)

	override val serialName: String
		get() = original.serialName
	override val kind: SerialKind
		get() = original.kind
	override val elementsCount: Int
		get() = original.elementsCount

	override fun getElementName(index: Int): String = original.getElementName(index)
	override fun getElementIndex(name: String): Int = original.getElementIndex(name)
	override fun getElementAnnotations(index: Int): List<Annotation> = original.getElementAnnotations(index)
	override fun getElementDescriptor(index: Int): SerialDescriptor = original.getElementDescriptor(index)
	override fun isElementOptional(index: Int): Boolean = original.isElementOptional(index)
}

open class SerializerCodec<T : Any>(private val serializer: KSerializer<T>) : Codec<T> {
	@Suppress("UNCHECKED_CAST")
	override fun <V : Any> encode(input: T, ops: DynamicOps<V>, prefix: V): DataResult<V> {
		return tryOrThrow<V> {
			val cod = SerializationManager[ops]
				?: throw UnsupportedOperationException("${ops::class.simpleName} is not a supported DynamicOps instance.")

			cod.encode(input, serializer as KSerializer<Any>) as V
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun <V : Any> decode(
		ops: DynamicOps<V>,
		input: V
	): DataResult<Pair<T, V>> {
		return tryOrThrow<Pair<T, V>> {
			val cod = SerializationManager[ops]
				?: throw UnsupportedOperationException("${ops::class.simpleName} is not a supported DynamicOps instance.")

			val value = cod.decode(input, serializer as KSerializer<Any>) as T

			Pair(value, input)
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

@Suppress("UNCHECKED_CAST")
open class CodecSerializer<T>(private val codec: Codec<T>) : KSerializer<T> {
	@OptIn(InternalSerializationApi::class)
	override val descriptor
		get() = buildSerialDescriptor("CodecSerializer", PolymorphicKind.SEALED) {
			SerializationManager.serializers.forEach {
				element(it.name ?: it.descriptor.serialName, defer { it.descriptor })
			}
		}

	override fun serialize(encoder: Encoder, value: T) {
		val ser = SerializationManager[encoder]
			?: throw UnsupportedOperationException("${encoder::class.simpleName} is not a supported serializer type.")

		ser.operation.encode(encoder, codec as Codec<Any>, value as Any)
	}

	override fun deserialize(decoder: Decoder): T {
		val ser = SerializationManager[decoder]
			?: throw UnsupportedOperationException("${decoder::class.simpleName} is not a supported serializer type.")

		return ser.operation.decode(decoder, codec as Codec<Any>) as T
	}
}

/**
 * Convert any [JsonElement] from kotlinx.serialization.json into [GsonElement] from gson
 */
val JsonElement.toGson: GsonElement
	get() = JsonParser.parseString(this.toString())

/**
 * Convert any [GsonElement] from gson into [JsonElement] kotlinx.serialization.json
 */
val GsonElement.toKson: JsonElement
	get() = SerializationManager.json.parseToJsonElement(this.toString())