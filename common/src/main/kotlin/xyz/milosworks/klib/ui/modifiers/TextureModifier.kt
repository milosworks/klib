package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import net.minecraft.resources.ResourceLocation

data class TextureModifier(val loc: ResourceLocation) : Modifier.Element<TextureModifier> {
	override fun mergeWith(other: TextureModifier): TextureModifier =
		throw UnsupportedOperationException("not implemented")
}

@Stable
fun Modifier.texture(loc: ResourceLocation) = this then TextureModifier(loc)