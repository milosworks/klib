package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.*
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.onGloballyPositioned
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.NinePatchThemeState
import xyz.milosworks.klib.ui.util.SimpleThemeState

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
        renderer = object : DefaultRenderer() {
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
                    (state as SimpleThemeState).texture,
                    x,
                    y,
                    state.width,
                    state.height,
                    state.u.toFloat(),
                    state.v.toFloat(),
                    state.textureSize.width,
                    state.textureSize.height,
                    state.uWidth,
                    state.vHeight
                )

                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
        },
        modifier = Modifier
            .debug("Texture: $texture")
            .onGloballyPositioned { data.slots.add(it) }.run {
                if (!composableTheme.isNinepatch) with(composableTheme.states["default"] as SimpleThemeState) {
                    sizeIn(
                        minWidth = width,
                        minHeight = height
                    )
                } else this
            } then modifier
    )
}