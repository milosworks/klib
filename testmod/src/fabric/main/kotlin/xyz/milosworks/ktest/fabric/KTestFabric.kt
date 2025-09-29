package xyz.milosworks.ktest.fabric

import net.fabricmc.api.ModInitializer
import xyz.milosworks.ktest.KTest

object KTestFabric : ModInitializer {
    override fun onInitialize() {
        KTest.init()
        KTest.LOGGER.info("Hello Fabric world from Kotlin TestMod!")
    }
}