package xyz.milosworks.ktest

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import xyz.milosworks.klib.KLib
import xyz.milosworks.ktest.init.TBlocks
import xyz.milosworks.ktest.init.TChannels
import xyz.milosworks.ktest.init.TCreativeTabs
import xyz.milosworks.ktest.init.TMenus

object KTest {
    const val ID = "${KLib.ID}_test"

    val LOGGER: Logger = LogUtils.getLogger()

    fun init() {
        TBlocks.init()
        TChannels.init()
        TCreativeTabs.init()
        TMenus.init()
    }

    fun initClient() {
        TMenus.initClient()
    }

    operator fun get(path: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(ID, path)
}