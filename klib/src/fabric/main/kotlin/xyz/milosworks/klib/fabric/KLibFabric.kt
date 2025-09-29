package xyz.milosworks.klib.fabric

import net.fabricmc.api.ModInitializer
import xyz.milosworks.klib.KLib

object KLibFabric : ModInitializer {
    override fun onInitialize() {
        KLib.init()
        KLib.LOGGER.info("Hello Fabric world from Kotlin!")
    }
}