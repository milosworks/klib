package xyz.milosworks.klib.networking

import net.minecraft.client.Minecraft
import net.minecraft.core.RegistryAccess
import net.minecraft.world.entity.player.Player

interface IPacketContext {
	val player: Player
	val registryAccess: RegistryAccess
	val minecraft: Minecraft get() = Minecraft.getInstance()
}