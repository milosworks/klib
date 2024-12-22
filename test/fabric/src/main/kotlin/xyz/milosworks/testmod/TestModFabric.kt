package xyz.milosworks.testmod

import TestMod
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer

object TestModFabric : ModInitializer, ClientModInitializer {
	override fun onInitialize() {
		TestMod.init()
		TestMod.initCommon()
	}

	override fun onInitializeClient() {
		TestMod.initClient()
	}
}