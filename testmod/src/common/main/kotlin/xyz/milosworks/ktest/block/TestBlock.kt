package xyz.milosworks.ktest.block

import dev.architectury.registry.menu.MenuRegistry
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import xyz.milosworks.ktest.init.TChannels.CHANNEL
import xyz.milosworks.ktest.networking.TPackets
import xyz.milosworks.ktest.ui.UIMenu

class TestBlock(props: Properties) : Block(props) {
    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val data = TPackets.Uwu(
            player.gameProfile.name,
            Int.MAX_VALUE,
            FriendlyByteBuf(Unpooled.buffer()).apply { writeUUID(player.uuid) },
            ResourceLocation.parse("minecraft:diamond_block"),
            Component.translatable("tooltips.testmod.uwu"),
            pos
        )

        if (level.isClientSide) {
            CHANNEL.toServer(data)

//			return InteractionResult.SUCCESS
        }

        if (player is ServerPlayer) {
            MenuRegistry.openMenu(
                player,
                SimpleMenuProvider(
                    { id, inventory, player -> UIMenu(id, inventory) },
                    Component.literal("")
                )
            )
        }

        return InteractionResult.SUCCESS
    }
}