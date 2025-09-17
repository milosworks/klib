package xyz.milosworks.klib.test.neoforge

import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import xyz.milosworks.klib.test.KTest
import xyz.milosworks.klib.test.init.TMenus

@Mod(KTest.ID)
object KTestNeoForge {
    init {
        MOD_BUS.addListener<RegisterMenuScreensEvent> {
            TMenus.initClient()
        }

        println(
            KTestNeoForge::class.java.classLoader.definedPackages.map(Package::getName).sorted()
                .forEach(::println)
        )

        MOD_BUS.addListener<FMLConstructModEvent> {
            KTest.init()
        }
        MOD_BUS.addListener<FMLClientSetupEvent> {
            KTest.initClient()
        }
        MOD_BUS.addListener<FMLCommonSetupEvent> {
            KTest.initCommon()
        }
    }
}