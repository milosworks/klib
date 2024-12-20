package xyz.milosworks.testmod.init

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import xyz.milosworks.klib.registry.BlockRegistryHelper
import xyz.milosworks.testmod.TestMod
import xyz.milosworks.testmod.block.Test

object TBlocks : BlockRegistryHelper<Block>(TestMod.ID) {
	val TEST by block("test") { Test(BlockBehaviour.Properties.of()) }
}