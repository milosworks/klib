package milosworks.testmod.networking

import com.mojang.serialization.Codec
import com.mojang.serialization.JavaOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import java.nio.ByteBuffer

val CHANNEL = NetworkChannel(Klib.id("main"))

data class TestData(
	val mystr2: String,
	val int: Int,
	val float: Float,
	val double: Double,
	val boolean: Boolean,
	val bufs: List<SecondTestData>,
	val mapped: Map<String, Int>,
	val loc: ResourceLocation
)

data class SecondTestData(
	val buf: ByteBuffer,
	val mappedInt: Map<Int, String>
)


val SecondTestCodec: MapCodec<SecondTestData> = RecordCodecBuilder.mapCodec {
	it.group(
		Codec.BYTE_BUFFER.fieldOf("buf").forGetter(SecondTestData::buf),
		Codec.unboundedMap(Codec.INT, Codec.STRING).fieldOf("mappedInt").forGetter(SecondTestData::mappedInt)
	).apply(it, ::SecondTestData)
}

val TestCodec: Codec<TestData> = RecordCodecBuilder.create {
	it.group(
		Codec.STRING.fieldOf("mystr").forGetter(TestData::mystr2),
		Codec.INT.fieldOf("int").forGetter(TestData::int),
		Codec.FLOAT.fieldOf("float").forGetter(TestData::float),
		Codec.DOUBLE.fieldOf("double").forGetter(TestData::double),
		Codec.BOOL.fieldOf("boolean").forGetter(TestData::boolean),
		SecondTestCodec.codec().listOf().fieldOf("bufs").forGetter(TestData::bufs),
		Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("mapped").forGetter(TestData::mapped),
		ResourceLocation.CODEC.fieldOf("loc").forGetter(TestData::loc)
	).apply(it, ::TestData)
}

//val STestCodec = TestSerializer.toCodec()

//object TestSerializer : KSerializer<TestData> {
//	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TestData") {
//		element<String>("str")
//		element<Int>("int")
//		element<Float>("float")
//		element<Double>("double")
//		element<Boolean>("boolean")
//	}
//
//	override fun serialize(encoder: Encoder, value: TestData) {
//		encoder.encodeStructure(descriptor) {
//			encodeStringElement(descriptor, 0, value.mystr2)
//			encodeIntElement(descriptor, 1, value.int)
//			encodeFloatElement(descriptor, 2, value.float)
//			encodeDoubleElement(descriptor, 3, value.double)
//			encodeBooleanElement(descriptor, 4, value.boolean)
//		}
//
//	}
//
//	override fun deserialize(decoder: Decoder): TestData {
//		return decoder.decodeStructure(descriptor) {
//			var str = ""
//			var int = 0
//			var float = 0f
//			var double = 0.0
//			var bool = false
//
//			while (true) {
//				when (val index = decodeElementIndex(descriptor)) {
//					0 -> str = decodeStringElement(descriptor, 0)
//					1 -> int = decodeIntElement(descriptor, 1)
//					2 -> float = decodeFloatElement(descriptor, 2)
//					3 -> double = decodeDoubleElement(descriptor, 3)
//					4 -> bool = decodeBooleanElement(descriptor, 4)
//					CompositeDecoder.DECODE_DONE -> break
//					else -> throw SerializationException("Unexpected index: $index")
//				}
//			}
//
//			TestData(str, int, float, double, bool)
//		}
//	}
//}

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
		val data = TestData(
			"uwu", Int.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, true,
			listOf(
				SecondTestData(
					ByteBuffer.allocate(10),
					mapOf(
						1 to "uwu"
					)
				)
			),
			mapOf(
				"uwu" to 1
			),
			ResourceLocation.parse("minecraft:diamond_block")
		)
		val encoded = TestCodec.encodeStart(JavaOps.INSTANCE, data)

		CHANNEL.serverbound(Uwu::class) { msg, ctx ->
			println(msg)
		}

		CHANNEL.clientbound(Uwu::class) { msg, ctx ->
			println(msg)
		}
	}
}