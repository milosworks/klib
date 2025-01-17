package xyz.milosworks.klib.test.init

import xyz.milosworks.klib.networking.NetworkChannel
import xyz.milosworks.klib.test.KTest
import xyz.milosworks.klib.test.networking.TPackets

object TChannels {
	val CHANNEL = NetworkChannel(KTest["main"])

	fun init() {
		TPackets.init()
		CHANNEL.register()
	}
}