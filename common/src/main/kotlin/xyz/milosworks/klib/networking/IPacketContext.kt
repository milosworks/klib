package xyz.milosworks.klib.networking

import net.minecraft.core.RegistryAccess
import net.minecraft.world.entity.player.Player

interface IPacketContext {
	val player: Player
	val registryAccess: RegistryAccess
}