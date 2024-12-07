package milosworks.klib.serialization

import com.mojang.serialization.Codec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

val <T : Any> Codec<T>.toPrimitiveKSerializer: KSerializer<T>?
	@Suppress("UNCHECKED_CAST")
	get() = when (this) {
		Codec.BOOL -> Boolean.serializer()
		Codec.BYTE -> Byte.serializer()
		Codec.SHORT -> Short.serializer()
		Codec.INT -> Int.serializer()
		Codec.LONG -> Long.serializer()
		Codec.FLOAT -> Float.serializer()
		Codec.DOUBLE -> Double.serializer()
		Codec.STRING -> String.serializer()
		Codec.BYTE_BUFFER -> ByteBufferSerializer
		Codec.INT_STREAM -> IntStreamSerializer
		Codec.LONG_STREAM -> LongStreamSerializer
		else -> null
	} as KSerializer<T>