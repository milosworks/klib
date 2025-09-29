package xyz.milosworks.klib.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import xyz.milosworks.klib.KLib

@Mod(KLib.ID)
class KLibNeoForge(modBus: IEventBus) {
    init {
        KLib.init()
        KLib.LOGGER.info("Hello NeoForge world from Kotlin!")
    }
}