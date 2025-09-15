package xyz.milosworks.klib

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(Constants.MOD_ID)
object KLib {
    init {
        Constants.LOG.info("Hello NeoForge world from Kotlin!")
    }
}