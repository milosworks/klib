package xyz.milosworks.klib.serialization.serializers

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import xyz.milosworks.klib.ui.argb
import java.awt.Color

/* ------------------ TypeAliases ------------------ */

typealias SColor = @Contextual Color

/* ------------------ Serializers ------------------ */

object ColorSerializer : KSerializer<Color> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
	override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.argb)
	override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt(), true)
}

val BuiltInSerializersModule = SerializersModule {
	contextual(Color::class, ColorSerializer)
}