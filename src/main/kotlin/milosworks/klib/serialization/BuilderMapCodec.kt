package milosworks.klib.serialization

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

interface IBuilderMapCodec {
	fun <T> builder(): RecordCodecBuilder<T, T>?
}

abstract class BuilderMapCodec<A> : MapCodec<A>() {
	open fun <T> builder(): RecordCodecBuilder<T, T>? = null
}