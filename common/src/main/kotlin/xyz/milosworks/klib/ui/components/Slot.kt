package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.*
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.onGloballyPositioned
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState

data class SlotData(
    val groups: MutableMap<String, SlotGroup> = mutableMapOf()
)

data class SlotGroup(
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var slots: MutableSet<IntCoordinates> = mutableSetOf()
)

val LocalSlotData = compositionLocalOf { SlotData() }
val LocalSlotGroup = compositionLocalOf { SlotGroup() }

@Composable
fun Slots(
    id: String,
    width: Int,
    height: Int,
    content: @Composable () -> Unit
): SlotGroup {
    val group = SlotGroup(
        0,
        0,
        width,
        height,
    )
    val data = LocalSlotData.current
    data.groups[id] = group

    Box(
        Modifier.onGloballyPositioned { coordinates ->
            val (x, y) = coordinates
            group.x = x
            group.y = y
            data.groups[id] = group
        }
    ) {
        CompositionLocalProvider(LocalSlotGroup provides group) {
            content()
        }
    }
    return group
}

@Composable
fun Slot(texture: String = "slot", modifier: Modifier = Modifier) {
    val data = LocalSlotGroup.current
    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)
    val state = composableTheme.getState(TextureStates.DEFAULT, theme.mode)

    Layout(
        measurePolicy = { _, constraints ->
            MeasureResult(constraints.minWidth, constraints.minHeight) {}
        },
        renderer = object : Renderer {
            override fun render(
                node: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                if (composableTheme.isNinepatch) return guiGraphics.ninePatchTexture(
                    x,
                    y,
                    node.width,
                    node.height,
                    state as NinePatchThemeState
                )

                guiGraphics.blit(
                    state.texture,
                    x,
                    y,
                    state.textureSize.width,
                    state.textureSize.height,
                    state.u.toFloat(),
                    state.v.toFloat(),
                    state.textureSize.width,
                    state.textureSize.height,
                    state.textureSize.width,
                    state.textureSize.height
                )
            }
        },
        modifier = Modifier.sizeIn(minWidth = 18, minHeight = 18).onGloballyPositioned { pos ->
            data.slots.add(pos)
        }.then(modifier)
    )
}