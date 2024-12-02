package vyrek.kodek.lib.serializer

import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.plus
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import net.minecraft.nbt.NbtOps

object SerializationManager {
	private var sharedModule: SerializersModule = SerializersModule {
		include(MinecraftSerializers.module)
	}

	var cbor: Cbor = createCbor()
	var json: Json = createJson()
	var nbt: Nbt = createNbt()

	infix fun overwrite(module: SerializersModule) {
		sharedModule = sharedModule overwriteWith module
		rebuild()
	}

	operator fun plus(module: SerializersModule) {
		sharedModule += module
		rebuild()
	}

	operator fun get(ops: DynamicOps<*>) = when (ops) {
		JsonOps.INSTANCE -> json
		NbtOps.INSTANCE -> nbt
		else -> null
	}

	private fun rebuild() {
		createCbor()
		createJson()
		createNbt()
	}

	private fun createCbor() = Cbor {
		serializersModule = sharedModule
	}

	private fun createJson() = Json {
		serializersModule = sharedModule
	}

	private fun createNbt() = Nbt {
		variant = NbtVariant.Java
		compression = NbtCompression.None
		serializersModule = sharedModule
	}
}