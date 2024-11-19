package vyrek.kodek.lib

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class NetworkChannel(id: ResourceLocation) {
	val packetId = CustomPacketPayload.Type<CustomPacketPayload>(id)
}