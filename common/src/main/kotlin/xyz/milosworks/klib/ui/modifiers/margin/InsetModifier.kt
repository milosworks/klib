package xyz.milosworks.klib.ui.modifiers.margin

import xyz.milosworks.klib.ui.modifiers.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.Modifier

data class InsetModifier(val inset: InsetValues) : Modifier.Element<OutsetModifier>, LayoutChangingModifier {
    override fun mergeWith(other: OutsetModifier): OutsetModifier {
        TODO("Not yet implemented")
    }
}