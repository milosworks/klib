package xyz.milosworks.klib.test.block

import dev.architectury.registry.menu.MenuRegistry
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import xyz.milosworks.klib.test.init.TChannels.CHANNEL
import xyz.milosworks.klib.test.init.TMenus.UI
import xyz.milosworks.klib.test.networking.TPackets

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
				object : MenuProvider {
					override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
						return UI.get().create(i, inventory)
					}

					override fun getDisplayName(): Component? {
						return Component.literal("")
					}
				}
			)
		}

		return InteractionResult.SUCCESS
	}
}