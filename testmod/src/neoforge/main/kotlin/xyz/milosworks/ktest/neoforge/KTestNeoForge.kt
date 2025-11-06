package xyz.milosworks.ktest.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import xyz.milosworks.ktest.KTest

@Mod(KTest.ID)
class KTestNeoForge(modBus: IEventBus) {
    init {
        modBus.addListener<FMLConstructModEvent> {
            KTest.init()
        }
        modBus.addListener<FMLClientSetupEvent> {
            KTest.initClient()
        }
    }
}