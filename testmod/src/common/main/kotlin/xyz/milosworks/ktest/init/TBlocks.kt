package xyz.milosworks.ktest.init

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import xyz.milosworks.klib.registry.BlockRegistryHelper
import xyz.milosworks.ktest.KTest
import xyz.milosworks.ktest.block.TestBlock

object TBlocks : BlockRegistryHelper<Block>(KTest.ID) {
    val TEST by block("test") { TestBlock(BlockBehaviour.Properties.of()) }
}