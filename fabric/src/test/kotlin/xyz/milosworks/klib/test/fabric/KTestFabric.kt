package xyz.milosworks.klib.test.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import xyz.milosworks.klib.test.KTest

object KTestFabric : ModInitializer, ClientModInitializer {
    override fun onInitialize() {
        KTest.init()
        KTest.initCommon()
    }

    override fun onInitializeClient() {
        KTest.initClient()
    }
}