package vyrek.kodek.block

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.neoforge.network.PacketDistributor
import vyrek.kodek.networking.TPackets

class Test(props: Properties): Block(props) {
	override fun useWithoutItem(
		state: BlockState,
		level: Level,
		pos: BlockPos,
		player: Player,
		hitResult: BlockHitResult
	): InteractionResult {
		if (player is ServerPlayer) PacketDistributor.sendToPlayer(player, TPackets.Uwu(player.gameProfile.name))

		return super.useWithoutItem(state, level, pos, player, hitResult)
	}
}