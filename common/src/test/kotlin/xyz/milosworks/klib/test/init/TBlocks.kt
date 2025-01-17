package xyz.milosworks.klib.test.init

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import xyz.milosworks.klib.registry.BlockRegistryHelper
import xyz.milosworks.klib.test.KTest
import xyz.milosworks.klib.test.block.TestBlock

object TBlocks : BlockRegistryHelper<Block>(KTest.ID) {
	val TEST by block("test") { TestBlock(BlockBehaviour.Properties.of()) }
}