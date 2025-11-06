package xyz.milosworks.ktest.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import xyz.milosworks.ktest.KTest

object KTestFabric : ModInitializer, ClientModInitializer {
    override fun onInitialize() {
        KTest.init()
    }

    override fun onInitializeClient() {
        KTest.initClient()
    }
}