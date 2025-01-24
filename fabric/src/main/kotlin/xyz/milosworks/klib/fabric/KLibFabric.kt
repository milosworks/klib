package xyz.milosworks.klib.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import xyz.milosworks.klib.KLib

/** @suppress suppress for dokka */
object KLibFabric : ModInitializer, ClientModInitializer {
	override fun onInitialize() {
		KLib.init()
		KLib.initCommon()
	}

	override fun onInitializeClient() {
		KLib.initClient()
	}
}