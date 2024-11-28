package vyrek.kodek.block

import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import vyrek.kodek.networking.CHANNEL
import vyrek.kodek.networking.TPackets

class Test(props: Properties) : Block(props) {
	override fun useWithoutItem(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		player: Player,
		hitResult: BlockHitResult
	): InteractionResult {
		if (level.isClientSide) {
			val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
				writeUUID(player.uuid)
			}

			CHANNEL.toServer(
				TPackets.Uwu(
					player.gameProfile.name,
					Int.MAX_VALUE,
					buf,
					ResourceLocation.parse("minecraft:diamond_block"),
//					Component.translatable("tooltips.testmod.uwu"),
					pos
				)
			)
		}

		return super.useWithoutItem(state, level, pos, player, hitResult)
	}
}