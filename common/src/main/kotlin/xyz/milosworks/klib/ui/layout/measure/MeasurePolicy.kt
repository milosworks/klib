package xyz.milosworks.klib.ui.layout.measure

import androidx.compose.runtime.Stable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.layout.primitive.IntOffset
import xyz.milosworks.klib.ui.layout.primitive.IntSize
import xyz.milosworks.klib.ui.modifiers.core.Constraints

interface MeasureScope

data class MeasureResult(
    val width: Int,
    val height: Int,
    val placer: Placer,
)

@Stable
fun interface MeasurePolicy {
    fun measure(scope: MeasureScope, measurables: List<Measurable>, constraints: Constraints): MeasureResult
}

@Stable
fun interface Placer {
    fun placeChildren()
}

@Stable
interface Renderer {
    fun render(node: UINode, x: Int, y: Int, guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {}
    fun renderAfterChildren(
        node: UINode,
        x: Int,
        y: Int,
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
    }
}

interface Measurable {
    fun measure(constraints: Constraints): Placeable
}

interface Placeable {
    var width: Int
    var height: Int

    fun placeAt(x: Int, y: Int)

    fun placeAt(offset: IntOffset) = placeAt(offset.x, offset.y)

    val size: IntSize get() = IntSize(width, height)
}