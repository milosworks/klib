package milosworks.klib.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.ByteBuffer
import java.util.stream.IntStream
import java.util.stream.LongStream

object ByteBufferSerializer : KSerializer<ByteBuffer> {
	override val descriptor: SerialDescriptor = SerialDescriptor("ByteBuffer", ByteArraySerializer().descriptor)

	override fun serialize(encoder: Encoder, value: ByteBuffer) {
		encoder.encodeSerializableValue(ByteArraySerializer(), value.array())
	}

	override fun deserialize(decoder: Decoder): ByteBuffer {
		return ByteBuffer.wrap(decoder.decodeSerializableValue(ByteArraySerializer()))
	}
}

object IntStreamSerializer : KSerializer<IntStream> {
	override val descriptor: SerialDescriptor = SerialDescriptor("IntStream", IntArraySerializer().descriptor)

	override fun serialize(encoder: Encoder, value: IntStream) {
		encoder.encodeSerializableValue(IntArraySerializer(), value.toArray())
	}

	override fun deserialize(decoder: Decoder): IntStream {
		return IntStream.of(*decoder.decodeSerializableValue(IntArraySerializer()))
	}
}

object LongStreamSerializer : KSerializer<LongStream> {
	override val descriptor: SerialDescriptor = SerialDescriptor("LongStream", LongArraySerializer().descriptor)

	override fun serialize(encoder: Encoder, value: LongStream) {
		encoder.encodeSerializableValue(LongArraySerializer(), value.toArray())
	}

	override fun deserialize(decoder: Decoder): LongStream {
		return LongStream.of(*decoder.decodeSerializableValue(LongArraySerializer()))
	}
}