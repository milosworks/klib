package xyz.milosworks.ktest.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import xyz.milosworks.ktest.KTest

@Mod(KTest.ID)
class KTestNeoForge(modBus: IEventBus) {
    init {
        KTest.init()
        KTest.LOGGER.info("Hello NeoForge world from Kotlin TestMod!")
    }
}