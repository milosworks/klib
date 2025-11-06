package xyz.milosworks.klib.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import xyz.milosworks.klib.KLib

@Mod(KLib.ID)
class KLibNeoForge(modBus: IEventBus) {
    init {
        modBus.addListener<FMLConstructModEvent> {
            KLib.init()
        }
        modBus.addListener<FMLClientSetupEvent> {
            KLib.initClient()
        }
    }
}