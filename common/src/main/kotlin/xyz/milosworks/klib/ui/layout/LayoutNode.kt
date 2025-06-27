package xyz.milosworks.klib.ui.layout

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.layout.measure.*
import xyz.milosworks.klib.ui.layout.primitive.IntCoordinates
import xyz.milosworks.klib.ui.layout.primitive.IntOffset
import xyz.milosworks.klib.ui.layout.primitive.IntSize
import xyz.milosworks.klib.ui.layout.primitive.Size
import xyz.milosworks.klib.ui.modifiers.DebugModifier
import xyz.milosworks.klib.ui.modifiers.appearance.BackgroundModifier
import xyz.milosworks.klib.ui.modifiers.appearance.GradientDirection
import xyz.milosworks.klib.ui.modifiers.appearance.OutlineModifier
import xyz.milosworks.klib.ui.modifiers.core.*
import xyz.milosworks.klib.ui.modifiers.layout.OnSizeChangedModifier
import xyz.milosworks.klib.ui.modifiers.position.inset.InsetModifier
import xyz.milosworks.klib.ui.modifiers.position.outset.OutsetModifier
import xyz.milosworks.klib.ui.utils.extensions.drawRectOutline
import xyz.milosworks.klib.ui.utils.extensions.fillGradient
import kotlin.reflect.KClass

// AARRGGBB
const val COMPONENT_OUTLINE = 0xFF00FFFF.toInt()
const val DEBUG_OUTLINE = 0xFF000000.toInt()
const val DEBUG_FILL = 0xA7000000.toInt()
const val DEBUG_TEXT = 0xFFFFFFFF.toInt()
const val OUTSET_FILL = 0x80800080.toInt()
const val INSET_FILL = 0x33FF9AA2.toInt()

const val lineSpacing = 2
const val columnSpacing = 6

internal class LayoutNode(
    private val nodeName: String = "LayoutNode",
) : Measurable, Placeable, UINode, MeasureScope {
    override var measurePolicy: MeasurePolicy = ChildMeasurePolicy
    override var renderer: Renderer = EmptyRenderer
    val children = mutableListOf<LayoutNode>()
    override var modifier: Modifier = Modifier
        set(value) {
            field = value
            processedModifier = modifier.foldIn(mutableMapOf()) { acc, element ->
                val existing = acc[element::class]
                if (existing != null)
                    acc[element::class] = existing.unsafeMergeWith(element)
                else
                    acc[element::class] = element
                acc
            }
            layoutChangingModifiers = modifier.foldIn(mutableListOf()) { acc, element ->
                if (element is LayoutChangingModifier) acc.add(element)
                acc
            }
        }
    var processedModifier = mapOf<KClass<out Modifier.Element<*>>, Modifier.Element<*>>()
    var layoutChangingModifiers: List<LayoutChangingModifier> = emptyList()

    inline fun <reified T : Modifier.Element<T>> get(): T? {
        return processedModifier[T::class] as T?
    }

    var parent: LayoutNode? = null

    override var width: Int = 0
    override var height: Int = 0
    override var x: Int = 0
    override var y: Int = 0

    private val absoluteCoords: IntCoordinates
        get() {
            var coordinates = IntCoordinates(this.x, this.y)
            var parent = this.parent
            while (parent != null) {
                coordinates += IntCoordinates(parent.x, parent.y)
                parent = parent.parent
            }
            return coordinates
        }

    val rootNode: LayoutNode get() = parent?.rootNode ?: this
    var debug: Boolean = false
        get() = parent?.debug ?: field
        set(value) = parent?.let { it.debug = value } ?: run { field = value }
    var extraDebug: Boolean = false
        get() = parent?.extraDebug ?: field
        set(value) = parent?.let { it.extraDebug = value } ?: run { field = value }

    override fun measure(constraints: Constraints): Placeable {
        val outset =
            children.fold(listOf<OutsetModifier>()) { acc, child -> acc + child.modifier.getAll<OutsetModifier>() }
        val horizontal = outset.fold(0) { horizontal, modifier -> modifier.horizontal + horizontal }
        val vertical = outset.fold(0) { vertical, modifier -> modifier.vertical + vertical }

        val innerConstraints =
            layoutChangingModifiers.fold(constraints) { inner, modifier ->
                modifier.modifyInnerConstraints(
                    inner
                )
            }

        val result = measurePolicy.measure(this, children, innerConstraints)

        val inset = this.modifier.get<InsetModifier>()
        val insetHorizontal = inset?.horizontal ?: 0
        val insetVertical = inset?.vertical ?: 0

        val newWidth = result.width + horizontal + insetHorizontal
        val newHeight = result.height + vertical + insetVertical

        if (width != newWidth || height != newHeight) {
            get<OnSizeChangedModifier>()?.onSizeChanged?.invoke(Size(newWidth, newHeight))
        }

        width = newWidth
        height = newHeight

        val layoutConstraints = layoutChangingModifiers.fold(constraints) { outer, modifier ->
            modifier.modifyLayoutConstraints(IntSize(newWidth, newHeight), outer)
        }

        width = width.coerceIn(layoutConstraints.minWidth..layoutConstraints.maxWidth)
        height = height.coerceIn(layoutConstraints.minHeight..layoutConstraints.maxHeight)

        result.placer.placeChildren()

        return object : Placeable by this {
            override var width: Int = this@LayoutNode.width
            override var height: Int = this@LayoutNode.height
        }
    }

    override fun placeAt(x: Int, y: Int) {
        val offset = layoutChangingModifiers.fold(IntOffset(x, y)) { acc, modifier ->
            modifier.modifyPosition(acc)
        }
        this.x = offset.x
        this.y = offset.y
    }

    override fun render(
        x: Int,
        y: Int,
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        if (parent == null) guiGraphics.pose().pushPose()

        val dx = this.x + x
        val dy = this.y + y
        renderer.render(this@LayoutNode, dx, dy, guiGraphics, mouseX, mouseY, partialTick)
        for (child in children) {
            val matrix = guiGraphics.pose().last().pose()

            // mx, my, mz
            guiGraphics.pose().translate(matrix.m30(), matrix.m31(), 1f)
            child.render(dx, dy, guiGraphics, mouseX, mouseY, partialTick)
        }
        renderer.renderAfterChildren(
            this@LayoutNode,
            dx,
            dy,
            guiGraphics,
            mouseX,
            mouseY,
            partialTick
        )

        if (parent == null) {
            if (rootNode.debug) {
                val matrix = guiGraphics.pose().last().pose()
                guiGraphics.pose().translate(matrix.m30(), matrix.m31(), 1f)
                renderDebug(x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
            guiGraphics.pose().popPose()
        }
    }

    private fun renderDebug(
        x: Int,
        y: Int,
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        val dx = this.x + x
        val dy = this.y + y

        val hoveredChildren = children.filter { it.isBounded(mouseX, mouseY) }
        if (hoveredChildren.isNotEmpty())
            return hoveredChildren.forEach {
                it.renderDebug(
                    dx,
                    dy,
                    guiGraphics,
                    mouseX,
                    mouseY,
                    partialTick
                )
            }

        if (!isBounded(mouseX, mouseY)) return

        guiGraphics.drawRectOutline(dx, dy, width, height, COMPONENT_OUTLINE)

        val outsetModifier = modifier.get<OutsetModifier>()
        outsetModifier?.let { mod ->
            with(mod.outset) {
//                if (top != 0 && bottom != 0 && left != 0 && right != 0) {
//                    guiGraphics.fill(dx - left, dy - top, dx + width + right, dy, OUTSET_FILL)
//                    guiGraphics.fill(dx - left, dy, dx, dy + height, OUTSET_FILL)
//                    guiGraphics.fill(dx + width + right, dy, dx + width, dy + height, OUTSET_FILL)
//                    guiGraphics.fill(dx - left, dy + height + bottom, dx + width + right, dy + height, OUTSET_FILL)
//
//                    return@let
//                }

                if (top != 0) {
                    guiGraphics.fill(dx, dy - top, dx + width, dy, OUTSET_FILL)
                }
                if (bottom != 0) {
                    guiGraphics.fill(dx, dy + height + bottom, dx + width, dy + height, OUTSET_FILL)
                }
                if (left != 0) {
                    guiGraphics.fill(dx - left, dy, dx, dy + height, OUTSET_FILL)
                }
                if (right != 0) {
                    guiGraphics.fill(dx + width + right, dy, dx + width, dy + height, OUTSET_FILL)
                }
            }
        }

        val debugStartX = dx + 1
        var debugStartY = dy + height + 1
        val minecraft = Minecraft.getInstance()
        val font = minecraft.font
        val lineHeight = font.lineHeight

        val debugLines: List<List<Component>> = buildList {
            add(listOf(Component.literal(nodeName)))
            add(
                listOf(
                    Component.literal("X:").apply {
                        append(Component.literal("$dx").withColor(0x00FFFF))
                        append(", Y:")
                        append(Component.literal("$dy").withColor(0x32CD32))
                    },
                    Component.literal("W:").apply {
                        append(Component.literal("$width").withColor(0xFFA500))
                        append(", H:")
                        append(Component.literal("$height").withColor(0x87CEEB))
                    }
                )
            )

            if (extraDebug) {
                val modList = mutableListOf<Component>()

                modifier.all { mod ->
                    if (mod is DebugModifier) {
                        modList.addAll(0, mod.toComponents())
                    } else modList.add(mod.toComponent())

                    true
                }

                if (modList.isNotEmpty()) {
                    add(listOf(Component.literal("Modifiers:")))

                    modList.forEach { add(listOf(it)) }
                }
            }
        }

        val lineWidths =
            debugLines.map { line -> line.sumOf { font.width(it) } + (line.size - 1) * columnSpacing }
        val maxLineWidth = lineWidths.maxOrNull()?.plus(4) ?: 0
        val debugHeight = (debugLines.size * (lineHeight + lineSpacing)) - lineSpacing + 2

        if (debugStartY + debugHeight > guiGraphics.guiHeight()) debugStartY -= height

        guiGraphics.drawRectOutline(
            debugStartX,
            debugStartY,
            maxLineWidth,
            debugHeight,
            DEBUG_OUTLINE
        )
        guiGraphics.fill(
            debugStartX,
            debugStartY,
            debugStartX + maxLineWidth,
            debugStartY + debugHeight,
            DEBUG_FILL
        )

        debugLines.forEachIndexed { rowIndex, line ->
            var currentX = debugStartX + 2
            val textY = debugStartY + rowIndex * (lineHeight + lineSpacing) + 2

            line.forEachIndexed { colIndex, text ->
                guiGraphics.drawString(font, text, currentX, textY, DEBUG_TEXT)
                if (colIndex < line.size - 1) {
                    currentX += font.width(text) + columnSpacing
                }
            }
        }
    }

    internal fun isBounded(mouseX: Int, mouseY: Int) =
        mouseX in absoluteCoords.x until (absoluteCoords.x + width) && mouseY in absoluteCoords.y until (absoluteCoords.y + height)

    override fun toString() = children.joinToString(prefix = "$nodeName(", postfix = ")")

    internal companion object {
        val ChildMeasurePolicy = MeasurePolicy { scope, measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            MeasureResult(placeables.maxOfOrNull { it.width } ?: 0,
                placeables.maxOfOrNull { it.height } ?: 0) {
                placeables.forEach { it.placeAt(0, 0) }
            }
        }
        private val ErrorMeasurePolicy = MeasurePolicy { _, _, _ -> error("Measurer not defined") }
    }
}

val EmptyRenderer = object : Renderer {}

open class DefaultRenderer : Renderer {
    lateinit var node: UINode

    val background: BackgroundModifier? by lazy {
        node.modifier.foldIn(null) { acc, el -> acc ?: el as? BackgroundModifier }
    }
    val outline: OutlineModifier? by lazy {
        node.modifier.foldIn(null) { acc, el -> acc ?: el as? OutlineModifier }
    }

    override fun render(
        uiNode: UINode,
        x: Int,
        y: Int,
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        node = uiNode

        background?.let {
            when (it.gradientDirection) {
                GradientDirection.TOP_TO_BOTTOM -> guiGraphics.fillGradient(
                    x,
                    y,
                    node.width,
                    node.height,
                    it.startColor,
                    it.startColor,
                    it.endColor,
                    it.endColor,
                )

                GradientDirection.LEFT_TO_RIGHT -> guiGraphics.fillGradient(
                    x,
                    y,
                    node.width,
                    node.height,
                    it.startColor,
                    it.endColor,
                    it.endColor,
                    it.startColor
                )

                GradientDirection.RIGHT_TO_LEFT -> guiGraphics.fillGradient(
                    x,
                    y,
                    node.width,
                    node.height,
                    it.endColor,
                    it.startColor,
                    it.startColor,
                    it.endColor
                )

                GradientDirection.BOTTOM_TO_TOP -> guiGraphics.fillGradient(
                    x,
                    y,
                    node.width,
                    node.height,
                    it.endColor,
                    it.endColor,
                    it.startColor,
                    it.startColor
                )
            }
        }

        outline?.let {
            guiGraphics.drawRectOutline(
                x, y, node.width, node.height, it.color
            )
        }
    }
}