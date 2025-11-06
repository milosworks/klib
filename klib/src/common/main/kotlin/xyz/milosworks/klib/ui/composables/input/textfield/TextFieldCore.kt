package xyz.milosworks.klib.ui.composables.input.textfield

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.util.Mth
import net.minecraft.util.StringUtil
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.primitive.Size
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.*
import kotlin.math.max

private const val BORDER_PADDING = 4 // Needed for mouse position calculation
private const val CURSOR_BLINK_INTERVAL_MS = 300L

/**
 * The internal state for a TextField, holding mutable properties like focus and scroll position.
 */
@Stable
class TextFieldState {
    var displayPos by mutableStateOf(0)

    var scrollY by mutableStateOf(0.0)
    var isFocused by mutableStateOf(false)
    var showCursor by mutableStateOf(false)
    var lastBlink by mutableStateOf(0L)
    var isDraggingScrollbar by mutableStateOf(false)

    internal var layoutInfo by mutableStateOf(Size(0, 0))

    fun onFocusChange(focused: Boolean) {
        if (isFocused != focused) {
            isFocused = focused
            if (focused) {
                lastBlink = System.currentTimeMillis()
                showCursor = true
            } else {
                showCursor = false
            }
        }
    }
}

@Composable
fun rememberTextFieldState(): TextFieldState = remember { TextFieldState() }

/**
 * A core composable that manages the state, focus, and input handling for a text field,
 * but delegates rendering to its content.
 *
 * @param value The current TextFieldValue.
 * @param onValueChange Callback for when the value changes.
 * @param font The font used for text measurement.
 * @param enabled Controls the enabled state.
 * @param readOnly Controls if the text can be changed.
 * @param singleLine If true, the text field is a single horizontal line.
 * @param maxLength The maximum number of characters allowed.
 * @param maxLines The maximum number of lines allowed (for multi-line fields).
 * @param content The composable content that receives the managed state for rendering.
 */
@Composable
fun TextFieldCore(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    font: Font,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    content: @Composable (state: TextFieldState) -> Unit
) {
    val state = rememberTextFieldState()

    LaunchedEffect(state.isFocused) {
        if (state.isFocused) {
            while (true) {
                val time = System.currentTimeMillis()
                if (time - state.lastBlink > CURSOR_BLINK_INTERVAL_MS) {
                    state.showCursor = !state.showCursor
                    state.lastBlink = time
                }
                delay(50)
            }
        } else {
            state.showCursor = false
        }
    }

    val scrollToCursor = {
        val (nodeWidth, nodeHeight) = state.layoutInfo
        if (nodeWidth <= 0 || nodeHeight <= 0) {
        } else if (singleLine) {
            val innerWidth = nodeWidth - BORDER_PADDING * 2
            val string = font.plainSubstrByWidth(value.text.substring(state.displayPos), innerWidth)
            val endPos = string.length + state.displayPos

            if (value.selection.start > endPos) {
                state.displayPos = value.selection.start - string.length
            } else if (value.selection.start <= state.displayPos) {
                state.displayPos = value.selection.start
            }
            state.displayPos = Mth.clamp(state.displayPos, 0, value.text.length)
        } else {
            val innerHeight = nodeHeight - BORDER_PADDING * 2
            val contentHeight = value.text.lines().size * font.lineHeight
            val maxScroll = max(0, contentHeight - innerHeight)

            val cursorLine = value.text.take(value.selection.start).count { it == '\n' }
            val cursorY = cursorLine * font.lineHeight

            if (cursorY < state.scrollY) {
                state.scrollY = cursorY.toDouble()
            }
            if (cursorY + font.lineHeight > state.scrollY + innerHeight) {
                state.scrollY = (cursorY + font.lineHeight - innerHeight).toDouble()
            }
            state.scrollY = Mth.clamp(state.scrollY, 0.0, maxScroll.toDouble())
        }
    }

    val onValueChangeAndScroll: (TextFieldValue) -> Unit = {
        onValueChange(it)
        scrollToCursor()
        state.showCursor = true
        state.lastBlink = System.currentTimeMillis()
    }

    Layout(
        measurePolicy = { _, _, constraints ->
            val width = constraints.maxWidth
            val height =
                if (singleLine) font.lineHeight + BORDER_PADDING * 2 else constraints.maxHeight
            state.layoutInfo = Size(width, height)
            MeasureResult(width, height) {}
        },
        modifier = modifier
            .onKeyEvent { _, event ->
                if (!enabled || !state.isFocused) {
                    return@onKeyEvent
                }

                if (event.keyCode == 256) { // ESC
                    state.onFocusChange(false)
                    event.consume(true)
                    return@onKeyEvent
                }

                var handled = true
                val result = when {
                    Screen.isSelectAll(event.keyCode) -> value.copy(
                        selection = TextRange(0, value.text.length)
                    )

                    Screen.isCopy(event.keyCode) -> {
                        Minecraft.getInstance().keyboardHandler.clipboard =
                            value.selectedText; value
                    }

                    Screen.isPaste(event.keyCode) -> if (!readOnly) {
                        value.handlePaste(maxLength, maxLines, singleLine)
                    } else value

                    Screen.isCut(event.keyCode) -> if (!readOnly) {
                        Minecraft.getInstance().keyboardHandler.clipboard = value.selectedText
                        value.deleteSelected()
                    } else value

                    !singleLine && !readOnly && (event.keyCode == 257 || event.keyCode == 335) -> {
                        if (value.text.lines().size < maxLines) value.insert("\n") else value
                    }

                    else -> {
                        val afterHandling = handleKey(event, value, singleLine, readOnly)
                        if (afterHandling == value) handled = false
                        afterHandling
                    }
                }

                if (result != value) onValueChangeAndScroll(result)
                if (handled) event.consume(true)
            }
            .onCharTyped { _, event ->
                if (enabled && !readOnly && state.isFocused && StringUtil.isAllowedChatCharacter(
                        event.codePoint
                    )
                ) {
                    val currentLength = value.text.length - value.selection.length
                    if (currentLength < maxLength) {
                        onValueChangeAndScroll(value.insert(event.codePoint.toString()))
                        event.consume(true)
                    }
                }
            }
            .onPointerEvent<LayoutNode>(PointerEventType.PRESS) { node, event ->
                if (state.isFocused && !node.isBounded(
                        event.mouseX.toInt(),
                        event.mouseY.toInt()
                    )
                ) state.onFocusChange(false)
            }
            .onPointerEvent<LayoutNode>(PointerEventType.PRESS) { node, event ->
                val (nodeX, nodeY) = node.absoluteCoords
                val (width, _) = state.layoutInfo
                val scrollbarX = nodeX + width - 8 // SCROLL_BAR_WIDTH
                if (!singleLine && event.mouseX >= scrollbarX && event.mouseX < nodeX + width) {
                    state.isDraggingScrollbar = true
                } else {
                    state.onFocusChange(true)
                    val localX = event.mouseX - nodeX - BORDER_PADDING
                    val localY = event.mouseY - nodeY - BORDER_PADDING
                    val cursorIndex =
                        findCursorPosFromPoint(
                            font,
                            value.text,
                            localX,
                            localY,
                            state,
                            singleLine
                        )
                    val selection = if (Screen.hasShiftDown()) TextRange(
                        value.selection.end,
                        cursorIndex
                    ) else TextRange(cursorIndex)
                    onValueChangeAndScroll(value.copy(selection = selection))
                }
                event.consume()
            }
            .onPointerEvent<UINode>(PointerEventType.RELEASE) { _, _ ->
                state.isDraggingScrollbar = false
            }
            .onDrag<LayoutNode> { node, event ->
                if (state.isDraggingScrollbar) {
                    val contentHeight = value.text.lines().size * font.lineHeight
                    val innerHeight = state.layoutInfo.height - BORDER_PADDING * 2
                    val scrollbarThumbHeight =
                        Mth.clamp((innerHeight * innerHeight) / contentHeight, 32, innerHeight)
                    val maxScroll = (contentHeight - innerHeight).coerceAtLeast(1)
                    val scrollPixelsPerDrag =
                        maxScroll.toDouble() / (innerHeight - scrollbarThumbHeight)
                    state.scrollY = Mth.clamp(
                        state.scrollY + event.dragY * scrollPixelsPerDrag,
                        0.0,
                        maxScroll.toDouble()
                    )
                } else if (state.isFocused) {
                    val (nodeX, nodeY) = node.absoluteCoords
                    val localX = event.mouseX - nodeX - BORDER_PADDING
                    val localY = event.mouseY - nodeY - BORDER_PADDING
                    val cursorIndex =
                        findCursorPosFromPoint(
                            font,
                            value.text,
                            localX,
                            localY,
                            state,
                            singleLine
                        )
                    onValueChangeAndScroll(
                        value.copy(
                            selection = TextRange(
                                value.selection.end,
                                cursorIndex
                            )
                        )
                    )
                }
                event.consume()
            }
            .onScroll<UINode> { _, event ->
                if (singleLine && !state.isFocused) return@onScroll

                val contentHeight = value.text.lines().size * font.lineHeight
                val innerHeight = state.layoutInfo.height - BORDER_PADDING * 2
                val maxScroll = (contentHeight - innerHeight).coerceAtLeast(0)

                state.scrollY = Mth.clamp(
                    state.scrollY - event.scrollY * font.lineHeight / 2.0,
                    0.0,
                    maxScroll.toDouble()
                )

                event.consume()
            }
    ) {
        content(state)
    }
}

private fun findCursorPosFromPoint(
    font: Font,
    text: String,
    x: Double,
    y: Double,
    state: TextFieldState,
    singleLine: Boolean
): Int {
    if (singleLine) {
        val visibleText = text.substring(state.displayPos)
        val clampedX = x.toInt().coerceAtLeast(0)
        return state.displayPos + font.plainSubstrByWidth(visibleText, clampedX).length
    } else {
        val scrolledY = y + state.scrollY
        val lineIndex = Mth.floor(scrolledY / font.lineHeight).coerceIn(0, text.lines().size - 1)
        val lineText = text.lines()[lineIndex]
        val clampedX = x.toInt().coerceAtLeast(0)
        val charIndexInLine = font.plainSubstrByWidth(lineText, clampedX).length
        return text.split('\n').take(lineIndex).sumOf { it.length + 1 } + charIndexInLine
    }
}

private fun handleKey(
    event: KeyEvent,
    value: TextFieldValue,
    singleLine: Boolean,
    readOnly: Boolean
): TextFieldValue {
    if (readOnly) {
        return when (event.keyCode) {
            262, 263, 264, 265, 268, 269 -> value.handleMovementKeys(event, singleLine)
            else -> value
        }
    }
    return value.handleMovementKeys(event, singleLine)
}

private fun TextFieldValue.handleMovementKeys(
    event: KeyEvent,
    singleLine: Boolean
): TextFieldValue {
    val shiftDown = Screen.hasShiftDown()
    val ctrlDown = Screen.hasControlDown()
    return when (event.keyCode) {
        259 -> deletePreviousChar(ctrlDown) // BACKSPACE
        261 -> deleteNextChar(ctrlDown)     // DELETE
        263 -> moveCursor(-1, shiftDown, ctrlDown) // LEFT
        262 -> moveCursor(1, shiftDown, ctrlDown)  // RIGHT
        265 -> if (!singleLine) moveCursorVertical(-1, shiftDown) else this // UP
        264 -> if (!singleLine) moveCursorVertical(1, shiftDown) else this  // DOWN
        268 -> moveCursorToLineStart(shiftDown) // HOME
        269 -> moveCursorToLineEnd(shiftDown)   // END
        else -> this
    }
}

private fun TextFieldValue.handlePaste(
    maxLength: Int,
    maxLines: Int,
    singleLine: Boolean
): TextFieldValue {
    val clipboard = Minecraft.getInstance().keyboardHandler.clipboard
    val currentLength = text.length - selection.length
    val availableLength = maxLength - currentLength
    var textToInsert =
        if (clipboard.length > availableLength) clipboard.take(availableLength) else clipboard

    if (!singleLine) {
        val currentLines = text.lines().size
        val linesInSelection = selectedText.count { it == '\n' }
        val availableNewlines = maxLines - (currentLines - linesInSelection)
        var newlineCount = 0
        textToInsert = buildString {
            for (char in textToInsert) {
                if (char == '\n') {
                    newlineCount++
                    if (newlineCount >= availableNewlines) break
                }
                append(char)
            }
        }
    }
    return insert(textToInsert)
}

private fun TextFieldValue.insert(textToInsert: String): TextFieldValue {
    val newText = text.take(selection.min) + textToInsert + text.substring(selection.max)
    val newCursorPos = selection.min + textToInsert.length
    return TextFieldValue(newText, TextRange(newCursorPos))
}

private fun TextFieldValue.deleteSelected(): TextFieldValue {
    if (selection.length == 0) return this
    val newText = text.take(selection.min) + text.substring(selection.max)
    return TextFieldValue(newText, TextRange(selection.min))
}

private fun TextFieldValue.deletePreviousChar(byWord: Boolean): TextFieldValue {
    if (selection.length > 0) return deleteSelected()
    if (selection.start == 0) return this
    val prevCursorPos =
        if (byWord) text.findLastWordBoundary(selection.start) else selection.start - 1
    val newText = text.take(prevCursorPos) + text.substring(selection.start)
    return TextFieldValue(newText, TextRange(prevCursorPos))
}

private fun TextFieldValue.deleteNextChar(byWord: Boolean): TextFieldValue {
    if (selection.length > 0) return deleteSelected()
    if (selection.start == text.length) return this
    val nextCursorPos =
        if (byWord) text.findNextWordBoundary(selection.start) else selection.start + 1
    val newText = text.take(selection.start) + text.substring(nextCursorPos)
    return TextFieldValue(newText, TextRange(selection.start))
}

private fun TextFieldValue.moveCursor(
    delta: Int,
    select: Boolean,
    byWord: Boolean
): TextFieldValue {
    val newPos = if (byWord) {
        if (delta < 0) text.findLastWordBoundary(selection.start) else text.findNextWordBoundary(
            selection.start
        )
    } else {
        (selection.start + delta).coerceIn(0, text.length)
    }
    val newSelection = if (select) TextRange(selection.end, newPos) else TextRange(newPos)
    return copy(selection = newSelection)
}

private fun TextFieldValue.moveCursorVertical(delta: Int, select: Boolean): TextFieldValue {
    val lines = text.lines()
    val cursorIndex = selection.start
    val currentLineIndex = text.take(cursorIndex).count { it == '\n' }
    val targetLineIndex = (currentLineIndex + delta).coerceIn(0, lines.lastIndex)
    if (currentLineIndex == targetLineIndex) return this
    val lastNewline = text.take(cursorIndex).lastIndexOf('\n')
    val posInLine = cursorIndex - (if (lastNewline == -1) 0 else lastNewline + 1)
    val targetLineStart = text.split('\n').take(targetLineIndex).sumOf { it.length + 1 }
    val newPos =
        (targetLineStart + posInLine).coerceAtMost(targetLineStart + lines[targetLineIndex].length)
    return copy(selection = if (select) TextRange(selection.end, newPos) else TextRange(newPos))
}

private fun TextFieldValue.moveCursorToLineStart(select: Boolean): TextFieldValue {
    val cursorIndex = selection.start
    val lastNewline = text.take(cursorIndex).lastIndexOf('\n')
    val currentLineStart = if (lastNewline == -1) 0 else lastNewline + 1
    return copy(
        selection = if (select) TextRange(selection.end, currentLineStart) else TextRange(
            currentLineStart
        )
    )
}

private fun TextFieldValue.moveCursorToLineEnd(select: Boolean): TextFieldValue {
    val cursorIndex = selection.start
    val nextNewline = text.indexOf('\n', cursorIndex)
    val currentLineEnd = if (nextNewline == -1) text.length else nextNewline
    return copy(
        selection = if (select) TextRange(selection.end, currentLineEnd) else TextRange(
            currentLineEnd
        )
    )
}

private fun String.findNextWordBoundary(from: Int): Int {
    var i = from
    val len = this.length
    while (i < len && this[i] == ' ') i++
    while (i < len && this[i] != ' ') i++
    return i
}

private fun String.findLastWordBoundary(from: Int): Int {
    var i = from - 1
    while (i >= 0 && this[i] == ' ') i--
    while (i >= 0 && this[i] != ' ') i--
    return i + 1
}
