package xyz.milosworks.klib.ui.modifiers.appearance

import androidx.compose.runtime.Stable
import net.minecraft.world.inventory.tooltip.TooltipComponent
import xyz.milosworks.klib.ui.modifiers.core.Modifier

data class TooltipModifier(val tooltips: List<TooltipComponent>) : Modifier.Element<TooltipModifier> {
    override fun mergeWith(other: TooltipModifier): TooltipModifier = TooltipModifier(tooltips + other.tooltips)
}

@Stable
fun Modifier.tooltip(vararg tooltips: TooltipComponent) = this then TooltipModifier(tooltips.toList())