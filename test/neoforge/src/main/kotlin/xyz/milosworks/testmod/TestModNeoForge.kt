package xyz.milosworks.testmod

import TestMod
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(TestMod.ID)
object TestModNeoForge {
	init {
		MOD_BUS.addListener<FMLConstructModEvent> {
			TestMod.init()
		}
		MOD_BUS.addListener<FMLClientSetupEvent> {
			TestMod.initCommon()
		}
		MOD_BUS.addListener<FMLCommonSetupEvent> {
			TestMod.initClient()
		}
	}
}