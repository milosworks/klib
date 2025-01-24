package xyz.milosworks.klib.neoforge

import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import xyz.milosworks.klib.KLib

/** @suppress suppress for dokka */
@Mod(KLib.ID)
object KLibNeoForge {
	init {
		MOD_BUS.addListener<FMLConstructModEvent> {
			KLib.init()
		}
		MOD_BUS.addListener<FMLClientSetupEvent> {
			KLib.initClient()
		}
		MOD_BUS.addListener<FMLCommonSetupEvent> {
			KLib.initCommon()
		}
	}
}