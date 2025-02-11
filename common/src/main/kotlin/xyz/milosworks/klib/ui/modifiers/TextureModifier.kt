package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import net.minecraft.resources.ResourceLocation

/**
 * A modifier that changes the texture used in certain components.
 *
 * This modifier allows a composable to override the default texture used in components like slots
 * or other UI elements that rely on textures. Applying this modifier will replace the component’s
 * texture with the specified [ResourceLocation].
 *
 * @param texture The [ResourceLocation] of the texture to be used.
 */
data class TextureModifier(val texture: ResourceLocation) : Modifier.Element<TextureModifier> {
	override fun mergeWith(other: TextureModifier): TextureModifier =
		throw UnsupportedOperationException("not implemented")
}

/**
 * Applies a custom texture to the component.
 *
 * This modifier allows you to change the texture of a component by providing a custom
 * [ResourceLocation] pointing to the desired texture. It is typically used with components
 * like Slot to modify their default texture.
 *
 * @param texture The [ResourceLocation] of the texture to be applied to the component.
 */
@Stable
fun Modifier.texture(texture: ResourceLocation) = this then TextureModifier(texture)