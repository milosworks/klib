package xyz.milosworks.klib.serialization

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import kotlinx.serialization.json.*
import net.benwoodworth.knbt.*
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NumericTag
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

/** @suppress */
object KOps {
    object Json : DynamicOps<JsonElement> {
        override fun empty(): JsonElement = JsonNull

        override fun <U : Any> convertTo(outOps: DynamicOps<U>, input: JsonElement): U {
            if (input is JsonObject)
                return convertMap(outOps, input)
            if (input is JsonArray)
                return convertList(outOps, input)
            if (input is JsonNull)
                return outOps.empty()
            val literal = input.jsonPrimitive
            if (literal.isString)
                return outOps.createString(literal.content)
            literal.booleanOrNull?.let { return outOps.createBoolean(it) }
            val decimal = BigDecimal(literal.content)
            try {
                return when (val long = decimal.longValueExact()) {
                    long.toByte().toLong() -> outOps.createByte(long.toByte())
                    long.toShort().toLong() -> outOps.createShort(long.toShort())
                    long.toInt().toLong() -> outOps.createInt(long.toInt())
                    else -> outOps.createLong(long)
                }
            } catch (e: ArithmeticException) {
                return when (val double = decimal.toDouble()) {
                    double.toFloat().toDouble() -> outOps.createFloat(double.toFloat())
                    else -> outOps.createDouble(double)
                }
            }
        }

        override fun getNumberValue(input: JsonElement): DataResult<Number> {
            val literal = input.jsonPrimitive
            try {
                val decimal = BigDecimal(literal.content)

                return try {
                    when (val long = decimal.longValueExact()) {
                        long.toByte().toLong() -> DataResult.success(long.toByte())
                        long.toShort().toLong() -> DataResult.success(long.toShort())
                        long.toInt().toLong() -> DataResult.success(long.toInt())
                        else -> DataResult.success(long)
                    }
                } catch (e: ArithmeticException) {
                    when (val double = decimal.toDouble()) {
                        double.toFloat().toDouble() -> DataResult.success(double.toFloat())
                        else -> DataResult.success(double)
                    }
                }
            } catch (e: NumberFormatException) {
                return DataResult.error { "Not a number: $input" }
            }
        }

        override fun createNumeric(i: Number): JsonElement {
            return JsonPrimitive(i)
        }

        override fun getBooleanValue(input: JsonElement): DataResult<Boolean> {
            return input.jsonPrimitive.booleanOrNull?.let { DataResult.success(it) }
                ?: DataResult.error { "Not a boolean: $input" }
        }

        override fun createBoolean(value: Boolean): JsonElement {
            return JsonPrimitive(value)
        }

        override fun getStringValue(input: JsonElement): DataResult<String> {
            val literal = input.jsonPrimitive
            return literal.contentOrNull?.takeIf { literal.isString }
                ?.let { DataResult.success(it) }
                ?: DataResult.error { "Not a string: $input" }
        }

        override fun createString(value: String): JsonElement {
            return JsonPrimitive(value)
        }

        override fun mergeToList(list: JsonElement, value: JsonElement): DataResult<JsonElement> {
            if (list !is JsonArray && list != empty())
                return DataResult.error({ "mergeToList called with not a list: $list" }, list)
            if (list != empty()) {
                return DataResult.success(JsonArray(list.jsonArray + value))
            }
            return DataResult.success(JsonArray(listOf(value)))
        }

        override fun mergeToMap(
            map: JsonElement,
            key: JsonElement,
            value: JsonElement
        ): DataResult<JsonElement> {
            if (map !is JsonObject && map != empty())
                return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
            if (key !is JsonPrimitive || !key.jsonPrimitive.isString)
                return DataResult.error({ "key is not a string: $key" }, map)
            if (map != empty()) {
                return DataResult.success(JsonObject(map.jsonObject + mapOf(key.content to value)))
            }
            return DataResult.success(JsonObject(mapOf(key.content to value)))
        }

        override fun getMapValues(input: JsonElement): DataResult<Stream<Pair<JsonElement, JsonElement>>> {
            if (input !is JsonObject) return DataResult.error { "Not a json object: $input" }
            return DataResult.success(
                input.entries.stream().map { (key, value) -> Pair(createString(key), value) })
        }

        override fun getMapEntries(input: JsonElement): DataResult<Consumer<BiConsumer<JsonElement, JsonElement>>> {
            if (input !is JsonObject) return DataResult.error { "Not a json object: $input" }
            return DataResult.success(Consumer { c ->
                input.entries.forEach { (key, value) -> c.accept(createString(key), value) }
            })
        }

        override fun getMap(input: JsonElement): DataResult<MapLike<JsonElement>> {
            if (input !is JsonObject) return DataResult.error { "Not a json object: $input" }
            return DataResult.success(object : MapLike<JsonElement> {
                override fun get(key: JsonElement): JsonElement? {
                    return input[key.jsonPrimitive.content]
                }

                override fun get(key: String): JsonElement? {
                    return input[key]
                }

                override fun entries(): Stream<Pair<JsonElement, JsonElement>> {
                    return input.entries.stream()
                        .map { (key, value) -> Pair(createString(key), value) }
                }
            })
        }

        override fun createMap(map: Stream<Pair<JsonElement, JsonElement>>): JsonElement {
            return JsonObject(
                map.map { it.first.jsonPrimitive.content to it.second }.toList().toMap()
            )
        }

        override fun getStream(input: JsonElement): DataResult<Stream<JsonElement>> {
            return if (input is JsonArray)
                DataResult.success(input.stream())
            else
                DataResult.error { "Not a json array: $input" }
        }

        override fun getList(input: JsonElement): DataResult<Consumer<Consumer<JsonElement>>> {
            return if (input is JsonArray)
                DataResult.success(Consumer { c ->
                    input.forEach {
                        c.accept(it)
                    }
                })
            else
                DataResult.error { "Not a json array: $input" }
        }

        override fun createList(input: Stream<JsonElement>): JsonElement {
            return JsonArray(input.toList())
        }

        override fun remove(input: JsonElement, key: String): JsonElement {
            if (input is JsonObject) {
                return JsonObject(input - key)
            }
            return input
        }
    }

    object Nbt : DynamicOps<NbtTag> {
        override fun empty(): NbtTag? = null

        override fun <U : Any> convertTo(outOps: DynamicOps<U>, input: NbtTag?): U {
            return when (input) {
                null -> outOps.empty()
                is NbtByte -> outOps.createByte(input.value)
                is NbtShort -> outOps.createShort(input.value)
                is NbtInt -> outOps.createInt(input.value)
                is NbtLong -> outOps.createLong(input.value)
                is NbtFloat -> outOps.createFloat(input.value)
                is NbtDouble -> outOps.createDouble(input.value)
                is NbtByteArray -> outOps.createByteList(ByteBuffer.wrap(input.toByteArray()))
                is NbtString -> outOps.createString(input.value)
                is NbtList<*> -> convertList(outOps, input)
                is NbtCompound -> convertMap(outOps, input)
                is NbtIntArray -> outOps.createIntList(Arrays.stream(input.toIntArray()))
                is NbtLongArray -> outOps.createLongList(Arrays.stream(input.toLongArray()))
            }
        }

        override fun getNumberValue(input: NbtTag): DataResult<Number> {
            return input.toMinecraftTag.takeIf { it is NumericTag }
                ?.let { DataResult.success((it as NumericTag).asNumber) }
                ?: DataResult.error { "Not a number" }
        }

        override fun createNumeric(i: Number): NbtTag {
            return NbtDouble(i.toDouble())
        }

        override fun createByte(value: Byte): NbtTag {
            return NbtByte(value)
        }

        override fun createShort(value: Short): NbtTag {
            return NbtShort(value)
        }

        override fun createInt(value: Int): NbtTag {
            return NbtInt(value)
        }

        override fun createLong(value: Long): NbtTag {
            return NbtLong(value)
        }

        override fun createFloat(value: Float): NbtTag {
            return NbtFloat(value)
        }

        override fun createDouble(value: Double): NbtTag {
            return NbtDouble(value)
        }

        override fun createBoolean(value: Boolean): NbtTag {
            return NbtByte(value)
        }

        override fun getStringValue(input: NbtTag): DataResult<String> {
            return input.takeIf { it is NbtString }?.nbtString?.let { DataResult.success(it.value) }
                ?: DataResult.error { "Not a string: $input" }
        }

        override fun createString(value: String): NbtTag {
            return NbtString(value)
        }

        private operator fun NbtList.Companion.invoke(content: List<NbtTag>): NbtList<*> =
            ListTag().apply { addAll(content.map { it.toMinecraftTag }) }.toKNbt!!

        override fun mergeToList(list: NbtTag?, value: NbtTag): DataResult<NbtTag> {
            if (list !is NbtList<*> && list != empty())
                return DataResult.error({ "mergeToList called with not a list: $list" }, list)
            if (list != empty()) {
                return DataResult.success(NbtList(list!!.nbtList + value))
            }
            return DataResult.success(NbtList(listOf(value)))
        }

        override fun mergeToMap(map: NbtTag?, key: NbtTag, value: NbtTag): DataResult<NbtTag> {
            if (map !is NbtCompound && map != empty())
                return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
            if (key !is NbtString)
                return DataResult.error({ "key is not a string: $key" }, map)
            if (map != empty()) {
                return DataResult.success(NbtCompound(map!!.nbtCompound + mapOf(key.value to value)))
            }
            return DataResult.success(NbtCompound(mapOf(key.value to value)))
        }

        override fun getMapValues(input: NbtTag): DataResult<Stream<Pair<NbtTag, NbtTag>>> {
            if (input !is NbtCompound) return DataResult.error { "Not an nbt compound: $input" }
            return DataResult.success(
                input.entries.stream().map { (key, value) -> Pair(createString(key), value) })
        }

        override fun getMapEntries(input: NbtTag): DataResult<Consumer<BiConsumer<NbtTag, NbtTag>>> {
            if (input !is NbtCompound) return DataResult.error { "Not an nbt compound: $input" }
            return DataResult.success(Consumer { c ->
                input.entries.forEach { (key, value) -> c.accept(createString(key), value) }
            })
        }

        override fun getMap(input: NbtTag): DataResult<MapLike<NbtTag>> {
            if (input !is NbtCompound) return DataResult.error { "Not an nbt compound: $input" }
            return DataResult.success(object : MapLike<NbtTag> {
                override fun get(key: NbtTag): NbtTag? {
                    return input[key.nbtString.value]
                }

                override fun get(key: String): NbtTag? {
                    return input[key]
                }

                override fun entries(): Stream<Pair<NbtTag, NbtTag>> {
                    return input.entries.stream()
                        .map { (key, value) -> Pair(createString(key), value) }
                }
            })
        }

        override fun createMap(map: Stream<Pair<NbtTag, NbtTag>>): NbtTag {
            return NbtCompound(map.toList().associate { it.first.nbtString.value to it.second })
        }

        override fun getStream(input: NbtTag): DataResult<Stream<NbtTag>> {
            return if (input is NbtList<*>)
                DataResult.success(input.stream())
            else
                DataResult.error { "Not an nbt list: $input" }
        }

        override fun getList(input: NbtTag): DataResult<Consumer<Consumer<NbtTag>>> {
            return if (input is NbtList<*>)
                DataResult.success(Consumer { c ->
                    input.forEach {
                        c.accept(it)
                    }
                })
            else
                DataResult.error { "Not an nbt list: $input" }
        }

        override fun createList(input: Stream<NbtTag>): NbtTag {
            return NbtList(input.toList())
        }

        override fun remove(input: NbtTag, key: String): NbtTag {
            if (input is NbtCompound) {
                return NbtCompound(input - key)
            }
            return input
        }

        override fun getByteBuffer(input: NbtTag): DataResult<ByteBuffer> {
            if (input is NbtByteArray) {
                return DataResult.success(ByteBuffer.wrap(input.toByteArray()))
            }
            return super.getByteBuffer(input)
        }

        override fun createByteList(input: ByteBuffer): NbtTag {
            val byteBuffer: ByteBuffer = input.duplicate().clear()
            val bs = ByteArray(input.capacity())
            byteBuffer[0, bs, 0, bs.size]
            return NbtByteArray(bs)
        }

        override fun getIntStream(input: NbtTag): DataResult<IntStream> {
            if (input is NbtIntArray) {
                return DataResult.success(Arrays.stream(input.toIntArray()))
            }
            return super.getIntStream(input)
        }

        override fun createIntList(input: IntStream): NbtTag {
            return NbtIntArray(input.toArray())
        }

        override fun getLongStream(input: NbtTag): DataResult<LongStream> {
            if (input is NbtLongArray) {
                return DataResult.success(Arrays.stream(input.toLongArray()))
            }
            return super.getLongStream(input)
        }

        override fun createLongList(input: LongStream): NbtTag {
            return NbtLongArray(input.toArray())
        }
    }
}