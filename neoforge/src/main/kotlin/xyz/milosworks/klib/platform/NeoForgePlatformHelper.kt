package xyz.milosworks.klib.platform

import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import xyz.milosworks.klib.platform.services.PlatformHelper

class NeoForgePlatformHelper : PlatformHelper {
    override fun getPlatformName(): String {
        return "NeoForge"
    }

    override fun isModLoaded(modId: String?): Boolean {
        return ModList.get().isLoaded(modId)
    }

    override fun isDevelopmentEnvironment(): Boolean {
        return !FMLLoader.isProduction()
    }
}