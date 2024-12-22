package xyz.milosworks.testmod.networking

import kotlinx.serialization.Contextual
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import xyz.milosworks.klib.serialization.CodecSerializer

typealias SComponent = @Contextual Component

val ComponentSerializer = CodecSerializer<Component>(ComponentSerialization.CODEC)