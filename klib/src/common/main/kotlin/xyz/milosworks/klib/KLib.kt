package xyz.milosworks.klib

import com.mojang.logging.LogUtils
import dev.architectury.registry.ReloadListenerRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import org.slf4j.Logger
import xyz.milosworks.klib.ui.utils.ThemeResourceListener

object KLib {
    const val ID = "klib"

    val LOGGER: Logger = LogUtils.getLogger()

    fun init() {
    }

    fun initClient() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, ThemeResourceListener())
    }

    operator fun get(path: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(ID, path)
}