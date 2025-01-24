package xyz.milosworks.klib.test

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer

object KTestFabric : ModInitializer, ClientModInitializer {
	override fun onInitialize() {
		KTest.init()
		KTest.initCommon()
	}

	override fun onInitializeClient() {
		KTest.initClient()
	}
}