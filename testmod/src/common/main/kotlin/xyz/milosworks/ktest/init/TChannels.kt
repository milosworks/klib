package xyz.milosworks.ktest.init

import xyz.milosworks.klib.networking.NetworkChannel
import xyz.milosworks.ktest.KTest
import xyz.milosworks.ktest.networking.TPackets

object TChannels {
    val CHANNEL = NetworkChannel(KTest["main"])

    fun init() {
        TPackets.init()
        CHANNEL.register()
    }
}