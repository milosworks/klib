package xyz.milosworks.klib.test

import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(KTest.ID)
object KLibNeoForge {
	init {
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