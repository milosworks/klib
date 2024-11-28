package vyrek.kodek.lib.serializer

import com.mojang.serialization.Codec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Util functions related to KSerializers
 */
object SerializerUtils {
	/**
	 * EXPERIMENTAL: Convert a minecraft codec into a KSerializer,
	 * its HIGHLY recommended to make a new KSerializer manually for the codec or
	 * use any of the already created KSerializers.
	 *
	 * @param codec Minecraft codec, it probably only works with MapCodec's (RecordCodecs)
	 */
	inline fun <reified T> fromCodec(codec: Codec<T>): KSerializer<T> {
		val name = T::class.simpleName ?: throw SerializationException("No name found for the codec class")

		return object : KSerializer<T> {
			override val descriptor: SerialDescriptor = buildClassSerialDescriptor(name)

			override fun serialize(encoder: Encoder, value: T) {
				val byteArrayOutputStream = ByteArrayOutputStream()
				val dataOutputStream = DataOutputStream(byteArrayOutputStream)

				val tag = codec.encodeStart(NbtOps.INSTANCE, value)
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

				val value = codec.decode(NbtOps.INSTANCE, tag)
					.getOrThrow { err -> throw SerializationException("Deserialization failed: $err") }

				return value.first as T
			}
		}
	}
}