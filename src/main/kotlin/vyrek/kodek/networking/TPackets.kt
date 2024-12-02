package vyrek.kodek.networking

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import vyrek.kodek.TestMod
import vyrek.kodek.lib.NetworkChannel
import vyrek.kodek.lib.serializer.SBlockPos
import vyrek.kodek.lib.serializer.SFriendlyByteBuf
import vyrek.kodek.lib.serializer.SResourceLocation

val CHANNEL = NetworkChannel(TestMod.id("main"))

@Serializable
data class TestData(
	val str: String,
	val int: Int,
	val float: Float,
	val double: Double,
	val boolean: Boolean
)


val TestCodec: Codec<TestData> = RecordCodecBuilder.create {
	it.group(
		Codec.STRING.fieldOf("str").forGetter(TestData::str),
		Codec.INT.fieldOf("int").forGetter(TestData::int),
		Codec.FLOAT.fieldOf("float").forGetter(TestData::float),
		Codec.DOUBLE.fieldOf("double").forGetter(TestData::double),
		Codec.BOOL.fieldOf("boolean").forGetter(TestData::boolean)
	).apply(it, ::TestData)
}

object TestSerializer : KSerializer<TestData> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TestData") {
		element<String>("str")
		element<Int>("int")
		element<Float>("float")
		element<Double>("double")
		element<Boolean>("boolean")
	}

	override fun serialize(encoder: Encoder, value: TestData) {
		encoder.encodeStructure(descriptor) {
			encodeStringElement(descriptor, 0, value.str)
			encodeIntElement(descriptor, 1, value.int)
			encodeFloatElement(descriptor, 2, value.float)
			encodeDoubleElement(descriptor, 3, value.double)
			encodeBooleanElement(descriptor, 4, value.boolean)
		}
	}

	override fun deserialize(decoder: Decoder): TestData {
		return decoder.decodeStructure(descriptor) {
			var str = ""
			var int = 0
			var float = 0f
			var double = 0.0
			var bool = false

			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> str = decodeStringElement(descriptor, 0)
					1 -> int = decodeIntElement(descriptor, 1)
					2 -> float = decodeFloatElement(descriptor, 2)
					3 -> double = decodeDoubleElement(descriptor, 3)
					4 -> bool = decodeBooleanElement(descriptor, 4)
					CompositeDecoder.DECODE_DONE -> break
					else -> throw SerializationException("Unexpected index: $index")
				}
			}

			TestData(str, int, float, double, bool)
		}
	}
}

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