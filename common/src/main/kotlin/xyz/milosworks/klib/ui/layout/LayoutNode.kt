package xyz.milosworks.klib.ui.layout

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import xyz.milosworks.klib.ui.extensions.drawRectOutline
import xyz.milosworks.klib.ui.extensions.fillGradient
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.modifiers.padding.PaddingModifier
import xyz.milosworks.klib.ui.nodes.UINode
import kotlin.reflect.KClass

const val COMPONENT_OUTLINE = 0xFF00FFFF.toInt()
const val DEBUG_OUTLINE = 0xFF000000.toInt()
const val DEBUG_FILL = 0xA7000000.toInt()
const val DEBUG_TEXT = 0xFFFFFFFF.toInt()

const val lineSpacing = 2
const val columnSpacing = 6

/**
 * TODO structure is really not decided on yet.
 *  I'd really like to avoid inheritance, and have only one ComposableNode call that creates Layout.
 *  You can configure stuff through [measurePolicy], [placer], and the [modifier], but things creates some problems
 *  when trying to make your own composable nodes that interact with this Layout node.
 */
internal class LayoutNode(
    private val nodeName: String = "LayoutNode",
) : Measurable, Placeable, UINode {
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
        set(value) = parent?.let { parent!!.debug = value } ?: run { field = value }
    var extraDebug: Boolean = false
        get() = parent?.extraDebug ?: field
        set(value) = parent?.let { parent!!.extraDebug = value } ?: run { field = value }

    private fun coercedConstraints(constraints: Constraints) = with(constraints) {
        object : Placeable by this@LayoutNode {
            override var width: Int = this@LayoutNode.width.coerceIn(minWidth..maxWidth).also {
                if (this@LayoutNode.modifier.contains<PaddingModifier>()) {
                    println(this@LayoutNode)
                    println(it)
                    println(minWidth)
                    println(maxWidth)
                }
            }
            override var height: Int = this@LayoutNode.height.coerceIn(minHeight..maxHeight)
        }
    }

    override fun measure(constraints: Constraints): Placeable {
        val innerConstraints = layoutChangingModifiers.fold(constraints) { inner, modifier ->
            modifier.modifyInnerConstraints(inner)
        }
        val result = measurePolicy.measure(children, innerConstraints)

        if (width != result.width || height != result.height) {
            get<OnSizeChangedModifier>()?.onSizeChanged?.invoke(Size(result.width, result.height))
        }
        if (modifier.contains<PaddingModifier>()) {
//            println()
        }
        width = result.width
        height = result.height
        result.placer.placeChildren()

        val layoutConstraints = layoutChangingModifiers.fold(constraints) { outer, modifier ->
            modifier.modifyLayoutConstraints(IntSize(result.width, result.height), outer)
        }
        if (modifier.contains<PaddingModifier>()) {
//            println()
        }
        // Returned constraints will always appear as though they are in parent's bounds
        return coercedConstraints(layoutConstraints)
    }

    override fun placeAt(x: Int, y: Int) {
        val offset = layoutChangingModifiers.fold(IntOffset(x, y)) { acc, modifier ->
            modifier.modifyPosition(acc)
        }
        this.x = offset.x
        this.y = offset.y
    }

    override fun render(x: Int, y: Int, guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (parent == null) guiGraphics.pose().pushPose()

        if (modifier.contains<PaddingModifier>()) {
            println()
        }

        val dx = this.x + x
        val dy = this.y + y
        renderer.render(this@LayoutNode, dx, dy, guiGraphics, mouseX, mouseY, partialTick)
        for (child in children) {
            val matrix = guiGraphics.pose().last().pose()

            // mx, my, mz
            guiGraphics.pose().translate(matrix.m30(), matrix.m31(), 1f)
            child.render(dx, dy, guiGraphics, mouseX, mouseY, partialTick)
        }
        renderer.renderAfterChildren(this@LayoutNode, dx, dy, guiGraphics, mouseX, mouseY, partialTick)

        if (parent == null) {
            if (rootNode.debug) {
                val matrix = guiGraphics.pose().last().pose()
                guiGraphics.pose().translate(matrix.m30(), matrix.m31(), 1f)
                renderDebug(x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
            guiGraphics.pose().popPose()
        }
    }

    private fun renderDebug(x: Int, y: Int, guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val dx = this.x + x
        val dy = this.y + y

        val hoveredChildren = children.filter { it.isBounded(mouseX, mouseY) }
        if (hoveredChildren.isNotEmpty())
            return hoveredChildren.forEach { it.renderDebug(dx, dy, guiGraphics, mouseX, mouseY, partialTick) }

        if (!isBounded(mouseX, mouseY)) return

        guiGraphics.drawRectOutline(dx, dy, width, height, COMPONENT_OUTLINE)

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

        val lineWidths = debugLines.map { line -> line.sumOf { font.width(it) } + (line.size - 1) * columnSpacing }
        val maxLineWidth = lineWidths.maxOrNull()?.plus(4) ?: 0
        val debugHeight = (debugLines.size * (lineHeight + lineSpacing)) - lineSpacing + 2

        if (debugStartY + debugHeight > guiGraphics.guiHeight()) debugStartY -= height

        guiGraphics.drawRectOutline(debugStartX, debugStartY, maxLineWidth, debugHeight, DEBUG_OUTLINE)
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

//	/**
//	 * @return Whether no elements were clickable or any element requested the bukkit click event to be cancelled.
//	 */
//	fun processClick(scope: ClickScope, x: Int, y: Int): ClickResult {
//		val cancelClickEvent = get<ClickModifier>()?.run {
//			onClick.invoke(scope)
//			cancelClickEvent
//		}
//		return children
//			.filter { x in it.x until (it.x + it.width) && y in it.y until (it.y + it.height) }
//			.fold(ClickResult(cancelClickEvent)) { acc, it ->
//				acc.mergeWith(it.processClick(scope, x - it.x, y - it.y))
//			}
//	}
//
//	data class DragInfo(
//		val dragModifier: DragModifier,
//		val itemMap: ItemPositions = ItemPositions(),
//	)
//
//	fun buildDragMap(coords: IntCoordinates, item: ItemStack, dragMap: MutableMap<GuiyNode, DragInfo>): Boolean {
//		val (iX, iY) = coords
//		if (iX !in 0 until width || iY !in 0 until height) return false
//		val dragModifier = get<DragModifier>()
//		if (dragModifier != null) {
//			val drag = dragMap.getOrPut(this) { DragInfo(dragModifier) }
//			dragMap[this] = drag.copy(itemMap = drag.itemMap.plus(iX, iY, item))
//			return true
//		}
//		return children.any { child ->
//			//TODO ensure this offset is still correct in x,y
//			val newCoords = IntCoordinates(iX - x + child.x, iY - x + child.x)
//			child.buildDragMap(newCoords, item, dragMap)
//		}
//	}
//
//	fun processDrag(scope: DragScope) {
//		val dragMap = mutableMapOf<GuiyNode, DragInfo>()
//		scope.updatedItems.items.forEach { (coords, item) ->
//			buildDragMap(coords, item, dragMap)
//		}
//		dragMap.forEach { (_, info) ->
//			info.dragModifier.onDrag.invoke(scope.copy(updatedItems = info.itemMap))
//		}
//	}

    override fun toString() = children.joinToString(prefix = "$nodeName(", postfix = ")")

    internal companion object {
        val ChildMeasurePolicy = MeasurePolicy { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            MeasureResult(placeables.maxOfOrNull { it.width } ?: 0, placeables.maxOfOrNull { it.height } ?: 0) {
                placeables.forEach { it.placeAt(0, 0) }
            }
        }
        private val ErrorMeasurePolicy = MeasurePolicy { _, _ -> error("Measurer not defined") }
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