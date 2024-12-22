package xyz.milosworks.testmod.init

import TestMod
import xyz.milosworks.klib.networking.NetworkChannel
import xyz.milosworks.testmod.networking.TPackets

object TChannels {
	val CHANNEL = NetworkChannel(TestMod["main"])

	fun init() {
		CHANNEL.register()
		TPackets.init()
	}
}