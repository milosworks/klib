package xyz.milosworks.klib.serialization

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.cbor.CborDecoder
import kotlinx.serialization.cbor.CborEncoder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.overwriteWith
import net.benwoodworth.knbt.*
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import xyz.milosworks.klib.serialization.SerializationManager.SerializationManagerBuilder.DynamicOpRegistryBuilder
import xyz.milosworks.klib.serialization.SerializationManager.SerializationManagerBuilder.SerializerRegistryBuilder
import xyz.milosworks.klib.serialization.serializers.BuiltInSerializersModule
import xyz.milosworks.klib.serialization.serializers.MinecraftSerializersModule
import kotlin.reflect.KClass
import com.google.gson.JsonElement as GsonElement

internal typealias EncodeDynamicOp<T> = (input: Any, strategy: KSerializer<Any>) -> T
internal typealias DecodeDynamicOp<T> = (input: T, strategy: KSerializer<Any>) -> Any

internal typealias EncodeSerializer<T> = (T, Codec<Any>, Any) -> Unit
internal typealias DecodeSerializer<T> = (T, Codec<Any>) -> Any

/**
 * Manages serializers for KLib, allowing customization and extension.
 *
 * ### Adding Serializers
 * KLib respects your serializer annotations, because of this you can use the `@Serializable()` annotation:
 * ```kotlin
 * data class MyData(@Serializable(MyClassSerializer::class) val field: MyClass)
 * ```
 * Otherwise, you can mark it as a `@Contextual` serializer and specify it in the [SerializationManager]:
 * ```kotlin
 * data class MyData(@Contextual val field: MyClass)
 *
 * val myModule = SerializersModule {
 *  contextual(MyClass::class, MyClassSerializer)
 * }
 *
 * SerializationManager {
 *  module {
 *   include(myModule)
 *  }
 * }
 * ```
 *
 * ### Overwriting Existing Serializers
 * To overwrite existing serializers:
 * ```kotlin
 * val myModule = SerializersModule {
 *  contextual(ResourceLocation::class, CustomResourceLocationSerializer)
 * }
 *
 * SerializationManager overwriteWith myModule
 * ```
 */
object SerializationManager {
	private var sharedModule: SerializersModule = SerializersModule {
		include(MinecraftSerializersModule)
		include(BuiltInSerializersModule)
	}

	private fun createCbor() = Cbor { serializersModule = sharedModule }
	private fun createJson() = Json { serializersModule = sharedModule }
	private fun createNbt() = Nbt {
		variant = NbtVariant.Java
		compression = NbtCompression.None
		serializersModule = sharedModule
	}

	var cbor: Cbor = createCbor()
		private set
	var json: Json = createJson()
		private set
	var nbt: Nbt = createNbt()
		private set

	private val ops: MutableMap<DynamicOps<*>, DynamicOpRegistryBuilder.Operation<Any>> =
		mutableMapOf()
	internal val serializers: MutableList<SerializerRegistryBuilder.Registry> = mutableListOf()

	private fun rebuild() {
		cbor = createCbor()
		json = createJson()
		nbt = createNbt()
	}

	/**
	 * Overwrites existing serializers with the provided module.
	 *
	 * @param module The new `SerializersModule` to use.
	 */
	infix fun overwriteWith(module: SerializersModule) {
		sharedModule = sharedModule overwriteWith module
		rebuild()
	}

	/**
	 * Retrieves the ops registry for a specific [DynamicOps] instance.
	 *
	 * @param op The [DynamicOps] instance.
	 * @return A registry that can encode/decode the `DynamicOp`, or `null` if not found.
	 */
	operator fun get(op: DynamicOps<*>) = ops[op]

	/**
	 * Retrieves the serializer registry for a specific [Encoder].
	 *
	 * @param encoder The [Encoder] interface.
	 * @return A registry that contains the [Encoder], or `null` if not found.
	 */
	operator fun get(encoder: Encoder) = serializers.find { it.operation.encoder.isInstance(encoder) }

	/**
	 * Retrieves the serializer registry for a specific [Decoder].
	 *
	 * @param decoder The [Decoder] interface.
	 * @return A registry that contains the [Decoder], or `null` if not found.
	 */
	operator fun get(decoder: Decoder) = serializers.find { it.operation.decoder.isInstance(decoder) }

	/**
	 * Configures the [SerializationManager]
	 */
	operator fun invoke(block: SerializationManagerBuilder.() -> Unit) {
		SerializationManagerBuilder().block()
		rebuild()
	}

	class SerializationManagerBuilder {
		/**
		 * Uses the [SerializersModuleBuilder] to configure the shared module between all serializer instances.
		 *
		 * @param block A builder function for creating a [SerializersModule]
		 */
		fun module(block: SerializersModuleBuilder.() -> Unit) {
			sharedModule = sharedModule.overwriteWith(SerializersModule { block() })
		}

		/**
		 * Registers a new [DynamicOps] instance with its associated encode and decode functions.
		 * This is used when you convert a [KSerializer] into a [Codec] via [SerializerCodec].
		 *
		 * ### Usage
		 * ```kotlin
		 * registerDynamicOp(JsonOps.INSTANCE) {
		 *  encode { input, strategy -> json.encodeToJsonElement(strategy, input).toGson }
		 *  decode { input, strategy ->
		 *   require(input is GsonElement) { "Expected input of type JsonElement but received ${input.javaClass.simpleName}." }
		 *
		 *   json.decodeFromJsonElement(strategy, input.toKson)
		 *  }
		 * }
		 * ```
		 *
		 * @param op The `DynamicOps` instance.
		 * @param block The builder function of the `DynamicOpRegistry`
		 */
		@Suppress("UNCHECKED_CAST")
		fun <T : Any> registerDynamicOp(
			op: DynamicOps<T>,
			block: DynamicOpRegistryBuilder<T>.() -> Unit
		) {
			val builder = DynamicOpRegistryBuilder<T>().apply(block)

			ops[op] = builder.build() as DynamicOpRegistryBuilder.Operation<Any>
		}

		/**
		 * Registers a new `Serializer` with its associated encode and decode functions.
		 * This is used when you convert a [Codec] into [KSerializer].
		 *
		 * **Note:** Because a limitation of the [Codec] structure you need an "intermediary" such as [JsonElement] or [NbtTag] for example.
		 *
		 * ### Usage
		 * ```kotlin
		 * registerSerializer(JsonElement.serializer().descriptor) {
		 *  encode(JsonEncoder::class) { encoder, codec, input ->
		 *   encoder.encodeJsonElement(codec.encodeStart(KOps.Json, input).orThrow)
		 *  }
		 *
		 *  decode(JsonDecoder::class) { decoder, codec ->
		 *   codec.parse(KOps.Json, decoder.decodeJsonElement()).orThrow
		 *  }
		 * }
		 * ```
		 *
		 * @param descriptor The descriptor of the serializer
		 * @param name An optional name for the element in the [CodecSerializer] descriptor
		 * @param block The builder function of the `SerializerRegistry`
		 */
		fun registerSerializer(
			descriptor: SerialDescriptor,
			name: String? = null,
			block: SerializerRegistryBuilder.() -> Unit
		) {
			val builder = SerializerRegistryBuilder().apply(block)

			serializers.add(
				SerializerRegistryBuilder.Registry(
					descriptor,
					name,
					builder.build()
				)
			)
		}

		class DynamicOpRegistryBuilder<T> {
			data class Operation<T>(
				val encode: EncodeDynamicOp<T>,
				val decode: DecodeDynamicOp<T>
			)

			private var encode: EncodeDynamicOp<T>? = null
			private var decode: DecodeDynamicOp<T>? = null

			/**
			 * Defines the encoder for this `DynamicOp`
			 *
			 * @param block The function used for encoding data
			 */
			fun encode(block: EncodeDynamicOp<T>) {
				encode = block
			}

			/**
			 * Defines the decoder for this `DynamicOp`
			 *
			 * @param block The function used for decoding data
			 */
			fun decode(block: DecodeDynamicOp<T>) {
				decode = block
			}

			internal fun build(): Operation<T> {
				requireNotNull(encode) { "Encode function must be provided before building the operation. Call `encode { ... }` to set it." }
				requireNotNull(decode) { "Decode function must be provided before building the operation. Call `decode { ... }` to set it." }

				return Operation<T>(encode!!, decode!!)
			}
		}

		class SerializerRegistryBuilder {
			data class Registry(
				val descriptor: SerialDescriptor,
				val name: String? = null,
				val operation: Operation
			)

			data class Operation(
				val encoder: KClass<out Encoder>,
				val decoder: KClass<out Decoder>,
				val encode: EncodeSerializer<Any>,
				val decode: DecodeSerializer<Any>
			)

			private var encoder: KClass<out Encoder>? = null
			private var encode: EncodeSerializer<Any>? = null

			private var decoder: KClass<out Decoder>? = null
			private var decode: DecodeSerializer<Any>? = null

			/**
			 * Defines the encoder for this `Serializer`
			 *
			 * @param block The function used for encoding data
			 */
			@Suppress("UNCHECKED_CAST")
			fun <T : Encoder> encode(enc: KClass<T>, block: EncodeSerializer<T>) {
				encoder = enc
				encode = block as EncodeSerializer<Any>
			}

			/**
			 * Defines the decoder for this `Serializer`
			 *
			 * @param block The function used for decoding data
			 */
			@Suppress("UNCHECKED_CAST")
			fun <T : Decoder> decode(dec: KClass<T>, block: DecodeSerializer<T>) {
				decoder = dec
				decode = block as DecodeSerializer<Any>
			}

			internal fun build(): Operation {
				requireNotNull(encoder) { "Encoder must be provided before building the operation. Call `encode(...) { ... }` to set it." }
				requireNotNull(encode) { "Encode function must be provided before building the operation. Call `encode(...) { ... }` to set it." }
				requireNotNull(decoder) { "Decoder must be provided before building the operation. Call `decode(...) { ... }` to set it." }
				requireNotNull(decode) { "Decode function must be provided before building the operation. Call `decode(...) { ... }` to set it." }

				return Operation(
					encoder!!,
					decoder!!,
					encode!!,
					decode!!
				)
			}
		}
	}

	init {
		SerializationManager {
			registerDynamicOp(JsonOps.INSTANCE) {
				encode { input, strategy -> json.encodeToJsonElement(strategy, input).toGson }
				decode { input, strategy ->
					require(input is GsonElement) { "Expected input of type JsonElement but received ${input.javaClass.simpleName}." }

					json.decodeFromJsonElement(strategy, input.toKson)
				}
			}

			registerDynamicOp(NbtOps.INSTANCE) {
				encode { input, strategy -> nbt.encodeToNbtTag(strategy, input).toMinecraftTag }
				decode { input, strategy ->
					require(input is Tag) { "Expected input of type Tag but received ${input.javaClass.simpleName}." }

					nbt.decodeFromNbtTag(
						strategy,
						input.toKNbt ?: throw IllegalStateException("Failed to convert a Minecraft Tag into a KNbtTag.")
					)
				}
			}

			registerSerializer(JsonElement.serializer().descriptor) {
				encode(JsonEncoder::class) { encoder, codec, input ->
					encoder.encodeJsonElement(codec.encodeStart(KOps.Json, input).orThrow)
				}

				decode(JsonDecoder::class) { decoder, codec ->
					codec.parse(KOps.Json, decoder.decodeJsonElement()).orThrow
				}
			}

			registerSerializer(NbtTag.serializer().descriptor, name = "NbtElement") {
				encode(NbtEncoder::class) { encoder, codec, input ->
					encoder.encodeNbtTag(codec.encodeStart(KOps.Nbt, input).orThrow)
				}

				decode(NbtDecoder::class) { decoder, codec ->
					codec.parse(KOps.Nbt, decoder.decodeNbtTag()).orThrow
				}
			}

			registerSerializer(String.serializer().descriptor, name = "CborElement") {
				encode(CborEncoder::class) { encoder, codec, input ->
					encoder.encodeString(codec.encodeStart(JsonOps.INSTANCE, input).orThrow.toString())
				}

				decode(CborDecoder::class) { decoder, codec ->
					val str = decoder.decodeString()
					codec.parse(JsonOps.INSTANCE, JsonParser.parseString(str)).orThrow
				}
			}
		}
	}
}