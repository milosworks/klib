package milosworks.klib.serialization

import net.benwoodworth.knbt.*
import net.minecraft.nbt.*

val NbtTag?.toMinecraftTag: Tag
	get() = when (this) {
		null -> EndTag.INSTANCE
		is NbtByte -> ByteTag.valueOf(value)
		is NbtByteArray -> ByteArrayTag(this)
		is NbtCompound -> toMinecraftTag
		is NbtDouble -> DoubleTag.valueOf(value)
		is NbtFloat -> FloatTag.valueOf(value)
		is NbtInt -> IntTag.valueOf(value)
		is NbtIntArray -> IntArrayTag(this)
		is NbtList<*> -> toMinecraftTag
		is NbtLong -> LongTag.valueOf(value)
		is NbtLongArray -> LongArrayTag(this)
		is NbtShort -> ShortTag.valueOf(value)
		is NbtString -> StringTag.valueOf(value)
	}

val NbtCompound.toMinecraftTag: CompoundTag
	get() = CompoundTag().apply {
		mapValues { it.value.toMinecraftTag }.forEach { (key, value) ->
			put(key, value)
		}
	}

val NbtList<*>.toMinecraftTag: ListTag
	get() = ListTag().apply {
		this@toMinecraftTag.map {
			it.toMinecraftTag
		}.forEach {
			add(it)
		}
	}

val Tag.toKNbt: NbtTag?
	get() = when (id.toInt()) {
		0 -> null
		1 -> NbtByte((this as NumericTag).asByte)
		2 -> NbtShort((this as NumericTag).asShort)
		3 -> NbtInt((this as NumericTag).asInt)
		4 -> NbtLong((this as NumericTag).asLong)
		5 -> NbtFloat((this as NumericTag).asFloat)
		6 -> NbtDouble((this as NumericTag).asDouble)
		7 -> NbtByteArray((this as ByteArrayTag).asByteArray)
		8 -> NbtString(this.asString)
		9 -> (this as ListTag).toKNbt
		10 -> (this as CompoundTag).toKNbt
		11 -> NbtIntArray((this as IntArrayTag).asIntArray)
		12 -> NbtLongArray((this as LongArrayTag).asLongArray)
		else -> throw IllegalStateException("Unknown tag type: $this")
	}

val CompoundTag.toKNbt: NbtCompound
	get() = buildNbtCompound {
		this@toKNbt.allKeys.associateWith {
			this@toKNbt[it]?.toKNbt
		}.forEach { (key, value) ->
			if (value != null)
				put(key, value)
		}
	}

val ListTag.toKNbt: NbtList<*>?
	get() = when (elementType.toInt()) {
		0 -> null
		1 -> buildNbtList { forEach { add(it.toKNbt as NbtByte) } }
		2 -> buildNbtList { forEach { add(it.toKNbt as NbtShort) } }
		3 -> buildNbtList { forEach { add(it.toKNbt as NbtInt) } }
		4 -> buildNbtList { forEach { add(it.toKNbt as NbtLong) } }
		5 -> buildNbtList { forEach { add(it.toKNbt as NbtFloat) } }
		6 -> buildNbtList { forEach { add(it.toKNbt as NbtDouble) } }
		7 -> buildNbtList { forEach { add(it.toKNbt as NbtByteArray) } }
		8 -> buildNbtList { forEach { add(it.toKNbt as NbtString) } }
		9 -> buildNbtList<NbtList<*>> { forEach { add(it.toKNbt as NbtList<*>) } }
		10 -> buildNbtList { forEach { add(it.toKNbt as NbtCompound) } }
		11 -> buildNbtList { forEach { add(it.toKNbt as NbtIntArray) } }
		12 -> buildNbtList { forEach { add(it.toKNbt as NbtLongArray) } }
		else -> throw IllegalStateException("Unknown tag type: $this")
	}