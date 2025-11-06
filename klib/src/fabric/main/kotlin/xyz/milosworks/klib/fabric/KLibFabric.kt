package xyz.milosworks.klib.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import xyz.milosworks.klib.KLib

object KLibFabric : ModInitializer, ClientModInitializer {
    override fun onInitialize() {
        KLib.init()
    }

    override fun onInitializeClient() {
        KLib.initClient()
    }
}