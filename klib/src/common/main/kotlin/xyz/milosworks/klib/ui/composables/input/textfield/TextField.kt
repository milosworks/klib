package xyz.milosworks.klib.ui.composables.input.textfield

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.utils.KColor
import kotlin.math.max
import kotlin.math.min

private data class WidgetSprites(val enabled: ResourceLocation, val highlighted: ResourceLocation) {
    fun get(active: Boolean, focused: Boolean): ResourceLocation {
        return if (active && focused) highlighted else enabled
    }
}

private val TEXT_FIELD_SPRITES = WidgetSprites(
    ResourceLocation.withDefaultNamespace("widget/text_field"),
    ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
)
private val SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller")
private const val BORDER_PADDING = 4
private const val SCROLL_BAR_WIDTH = 8
private const val SELECTION_COLOR_ARGB = -16776961 // Opaque Blue
private const val CURSOR_COLOR_ARGB = -3092272 // Gray

/**
 * A BasicTextField is a simpler version of [TextField] which manages its own state internally.
 */
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textColor: KColor = KColor.ofRgb(0xE0E0E0),
    cursorColor: KColor = KColor.ofRgb(CURSOR_COLOR_ARGB),
    selectionColor: KColor = KColor.ofRgb(SELECTION_COLOR_ARGB),
    font: Font = Minecraft.getInstance().font,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    var textFieldValue by remember(value) { mutableStateOf(TextFieldValue(value)) }
    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onValueChange(it.text)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textColor = textColor,
        cursorColor = cursorColor,
        selectionColor = selectionColor,
        font = font,
        singleLine = singleLine,
        maxLength = maxLength,
        maxLines = maxLines
    )
}

/**
 * A fully controlled TextField composable that displays editable text with the default Minecraft appearance.
 */
@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textColor: KColor = KColor.ofRgb(0xE0E0E0),
    cursorColor: KColor = KColor.ofRgb(CURSOR_COLOR_ARGB),
    selectionColor: KColor = KColor.ofRgb(SELECTION_COLOR_ARGB),
    font: Font = Minecraft.getInstance().font,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    TextFieldCore(
        value = value,
        onValueChange = onValueChange,
        font = font,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLength = maxLength,
        maxLines = maxLines
    ) { state ->
        Layout(
            measurePolicy = { _, _, constraints ->
                val width = constraints.maxWidth
                val height =
                    if (singleLine) font.lineHeight + BORDER_PADDING * 2 else constraints.maxHeight
                MeasureResult(width, height) {}
            },
            renderer = object : Renderer {
                override fun render(
                    node: UINode, x: Int, y: Int, guiGraphics: GuiGraphics,
                    mouseX: Int, mouseY: Int, partialTick: Float
                ) {
                    val (width, height) = state.layoutInfo
                    if (width <= 0 || height <= 0) return

                    val sprite = TEXT_FIELD_SPRITES.get(enabled, state.isFocused)
                    guiGraphics.blitSprite(sprite, x, y, width, height)

                    val contentWidth = width - BORDER_PADDING * 2
                    val contentHeight = height - BORDER_PADDING * 2
                    val contentX = x + BORDER_PADDING
                    val contentY = y + BORDER_PADDING
                    val lines = value.text.lines()

                    guiGraphics.enableScissor(
                        contentX,
                        contentY,
                        contentX + contentWidth,
                        contentY + contentHeight
                    )
                    guiGraphics.pose().pushPose()
                    guiGraphics.pose().translate(contentX.toDouble(), contentY.toDouble(), 0.0)

                    if (singleLine) {
                        renderSingleLineContent(
                            guiGraphics,
                            value,
                            font,
                            state,
                            contentWidth,
                            textColor.argb,
                            if (state.showCursor && state.isFocused) cursorColor.argb else 0,
                            selectionColor.argb
                        )
                    } else {
                        guiGraphics.pose().translate(0.0, -state.scrollY, 0.0)
                        renderMultiLineContent(
                            guiGraphics,
                            value,
                            font,
                            lines,
                            if (state.showCursor && state.isFocused) cursorColor.argb else 0,
                            selectionColor.argb,
                            textColor.argb
                        )
                    }

                    guiGraphics.pose().popPose()
                    guiGraphics.disableScissor()

                    if (!singleLine) {
                        val contentHeightVal = lines.size * font.lineHeight
                        val innerHeight = height - BORDER_PADDING * 2
                        if (contentHeightVal > innerHeight) {
                            renderScrollBar(
                                guiGraphics,
                                x + width - SCROLL_BAR_WIDTH,
                                y,
                                height,
                                contentHeightVal,
                                state.scrollY
                            )
                        }
                    }
                }
            }
        )
    }
}

private fun renderSingleLineContent(
    gui: GuiGraphics, value: TextFieldValue, font: Font, state: TextFieldState, width: Int,
    textColor: Int, cursorColor: Int, selectionColor: Int
) {
    val text = value.text
    val selection = value.selection
    val visibleText = font.plainSubstrByWidth(text.substring(state.displayPos), width)
    gui.drawString(font, visibleText, 0, 0, textColor)

    if (selection.length > 0) {
        val start = Mth.clamp(selection.min, 0, text.length)
        val end = Mth.clamp(selection.max, 0, text.length)
        if (start >= state.displayPos || end >= state.displayPos) {
            val selStartInView = (start - state.displayPos).coerceAtLeast(0)
            val selEndInView = (end - state.displayPos).coerceAtLeast(0)
            val visiblePart = text.substring(state.displayPos)
            val startX =
                font.width(visiblePart.take(selStartInView.coerceAtMost(visiblePart.length)))
            val endX = font.width(visiblePart.take(selEndInView.coerceAtMost(visiblePart.length)))
            gui.fill(
                RenderType.guiTextHighlight(),
                startX,
                -1,
                endX,
                font.lineHeight,
                selectionColor
            )
        }
    }

    if (cursorColor != 0 && selection.isCollapsed) {
        val cursorIndex = selection.start
        if (cursorIndex >= state.displayPos) {
            val cursorX = font.width(text.substring(state.displayPos, cursorIndex))
            gui.fill(cursorX, -1, cursorX + 1, font.lineHeight, cursorColor)
        }
    }
}

private fun renderMultiLineContent(
    gui: GuiGraphics,
    value: TextFieldValue,
    font: Font,
    lines: List<String>,
    cursorColor: Int,
    selectionColor: Int,
    textColor: Int
) {
    val text = value.text
    val selection = value.selection
    var yOffset = 0
    var charIndex = 0

    for (line in lines) {
        gui.drawString(font, line, 0, yOffset, textColor)
        if (selection.length > 0) {
            val lineStart = charIndex
            val lineEnd = lineStart + line.length
            val selStart = selection.min
            val selEnd = selection.max
            if (selStart <= lineEnd && selEnd >= lineStart) {
                val startInLine = max(selStart, lineStart) - lineStart
                val endInLine = min(selEnd, lineEnd) - lineStart
                val startX = font.width(line.take(startInLine))
                val endX = font.width(line.take(endInLine))
                gui.fill(
                    RenderType.guiTextHighlight(),
                    startX,
                    yOffset,
                    endX,
                    yOffset + font.lineHeight,
                    selectionColor
                )
            }
        }
        yOffset += font.lineHeight
        charIndex += line.length + 1
    }

    if (cursorColor != 0 && selection.isCollapsed) {
        val cursorIndex = selection.start
        val textBeforeCursor = text.take(cursorIndex)
        val lineIndex = textBeforeCursor.count { it == '\n' }
        val lastNewline = textBeforeCursor.lastIndexOf('\n')
        val colIndex = cursorIndex - (if (lastNewline == -1) 0 else lastNewline + 1)
        if (lineIndex < lines.size) {
            val cursorX = font.width(
                lines[lineIndex].substring(
                    0,
                    colIndex.coerceAtMost(lines[lineIndex].length)
                )
            )
            val cursorY = lineIndex * font.lineHeight
            gui.fill(cursorX, cursorY, cursorX + 1, cursorY + font.lineHeight, cursorColor)
        }
    }
}

private fun renderScrollBar(
    gui: GuiGraphics,
    x: Int,
    y: Int,
    nodeHeight: Int,
    contentHeight: Int,
    scrollY: Double
) {
    val innerHeight = nodeHeight - BORDER_PADDING * 2
    val scrollbarThumbHeight =
        Mth.clamp((innerHeight * innerHeight) / contentHeight, 32, innerHeight)
    val maxScroll = (contentHeight - innerHeight).coerceAtLeast(1)
    val scrollbarY = y + BORDER_PADDING + Mth.clamp(
        (scrollY * (innerHeight - scrollbarThumbHeight)) / maxScroll,
        0.0,
        (innerHeight - scrollbarThumbHeight).toDouble()
    ).toInt()
    gui.blitSprite(SCROLLER_SPRITE, x, scrollbarY, SCROLL_BAR_WIDTH, scrollbarThumbHeight)
}
