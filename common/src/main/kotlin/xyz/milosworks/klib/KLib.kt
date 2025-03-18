package xyz.milosworks.klib

import com.mojang.logging.LogUtils
import dev.architectury.platform.Mod
import dev.architectury.platform.Platform
import dev.architectury.registry.ReloadListenerRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import org.slf4j.Logger
import xyz.milosworks.klib.ui.util.ThemeResourceListener


/** @suppress suppress for dokka */
object KLib {
    const val ID = "klib"

    @JvmField
    val MOD: Mod = Platform.getMod(ID)

    @JvmField
    val LOGGER: Logger = LogUtils.getLogger()

    @JvmStatic
    operator fun get(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ID, path)

    @JvmStatic
    fun init() {
    }

    @JvmStatic
    fun initClient() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, ThemeResourceListener())
    }

    @JvmStatic
    fun initCommon() {
    }
}