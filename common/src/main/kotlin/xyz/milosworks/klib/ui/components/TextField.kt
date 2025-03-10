package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import net.minecraft.util.StringUtil
import org.lwjgl.glfw.GLFW
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.SizeModifier
import xyz.milosworks.klib.ui.modifiers.get
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onCharTyped
import xyz.milosworks.klib.ui.modifiers.input.onKeyEvent
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.sizeIn
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.util.KColor
import xyz.milosworks.klib.ui.util.NinePatchThemeState
import xyz.milosworks.klib.ui.util.SimpleThemeState
import kotlin.math.absoluteValue
import kotlin.math.floor

const val innerPadding = 8

// TODO: Add multiline support
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    maxLength: Int = 32,
//	singleLine: Boolean = false,
//	maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
//	minLines: Int = 1,
    font: Font = Minecraft.getInstance().font,
    texture: String = "text_field"
) {
    var cursorPos: Int by remember { mutableStateOf(0) }
    var highlightPos: Int by remember { mutableStateOf(0) }
    var displayPos: Int by remember { mutableStateOf(0) }
    var focused: Boolean by remember { mutableStateOf(false) }
    var focusedTime: Long by remember { mutableStateOf(Util.getMillis()) }

    var sizeModifier by remember { mutableStateOf(modifier.get<SizeModifier>()) }
    requireNotNull(sizeModifier) { "TextField requires a SizeModifier" }
    require(sizeModifier!!.constraints.minWidth == sizeModifier!!.constraints.maxWidth) { "TextField does not support content-sizing on width" }

    val theme = LocalTheme.current
    val composableTheme = theme.getComposableTheme(texture)

    val innerWidth = sizeModifier!!.constraints.maxWidth - innerPadding

    var lastValue by remember { mutableStateOf(value) }
    var targetCursorPos by remember { mutableStateOf(cursorPos) }

    fun scrollTo(pos: Int) {
        displayPos = minOf(displayPos, value.length)
        val visibleText = font.plainSubstrByWidth(value.substring(displayPos), innerWidth)
        val visibleEnd = visibleText.length + displayPos

        if (pos == displayPos) displayPos -= font.plainSubstrByWidth(value, innerWidth, true).length
        else if (pos > visibleEnd) displayPos += pos - visibleEnd
        else if (pos < displayPos) displayPos = pos

        displayPos = displayPos.coerceIn(0, value.length)
    }

    fun setCursor(pos: Int) {
        cursorPos = pos.coerceIn(0, value.length)
        scrollTo(cursorPos)
    }

    fun setHighlight(pos: Int) {
        highlightPos = pos.coerceIn(0, value.length)
        scrollTo(highlightPos)
    }

    fun setText(text: String) {
        val start = cursorPos.coerceAtMost(highlightPos)
        val end = cursorPos.coerceAtLeast(highlightPos)
        var availableSpace = maxLength - value.length - (start - end)
        if (availableSpace <= 0) return

        var str = StringUtil.filterText(text)
        if (availableSpace < str.length) {
            if (str[availableSpace - 1].isHighSurrogate()) availableSpace--
            str = str.substring(0, availableSpace)
        }

        val newStr = buildString {
            append(value)
            replace(start, end, str)
        }

        targetCursorPos = start + str.length
        onValueChange(newStr)
    }

    fun getWordPos(num: Int, pos: Int = cursorPos, skipSpaces: Boolean = true): Int {
        var currentPosition = pos
        val isMovingBackward = num < 0
        val absNumWords = num.absoluteValue

        repeat(absNumWords) {
            if (isMovingBackward) {
                while (currentPosition > 0 && skipSpaces && value[currentPosition - 1] == ' ') {
                    currentPosition--
                }
                while (currentPosition > 0 && value[currentPosition - 1] != ' ') {
                    currentPosition--
                }
            } else {
                currentPosition = value.indexOf(' ', currentPosition)
                if (currentPosition == -1) {
                    currentPosition = value.length
                } else if (skipSpaces) {
                    while (currentPosition < value.length && value[currentPosition] == ' ') {
                        currentPosition++
                    }
                }
            }
        }
        return currentPosition
    }

    fun removeCharsToPos(count: Int) {
        if (value.isEmpty()) return
        if (highlightPos != cursorPos) return setText("")
        val start = count.coerceAtMost(cursorPos)
        val end = count.coerceAtLeast(cursorPos)
        if (start == end) return

        val newValue = value.removeRange(start, end)
        targetCursorPos = start
        onValueChange(newValue)
    }

    fun removeChars(count: Int) = removeCharsToPos(Util.offsetByCodepoints(value, cursorPos, count))

    fun removeWords(count: Int) {
        if (value.isEmpty()) return
        if (highlightPos != cursorPos) setText("")
        else removeCharsToPos(getWordPos(count))
    }

    fun removeText(count: Int) =
        if (Screen.hasControlDown()) removeWords(count) else removeChars(count)

    LaunchedEffect(value) {
        if (value != lastValue) {
            val newPos = if (value.length >= lastValue.length) targetCursorPos.coerceAtMost(value.length)
            else value.commonPrefixWith(lastValue).length

            if (cursorPos != newPos) {
                setCursor(newPos)
                setHighlight(newPos)
                targetCursorPos = newPos
            }

            lastValue = value
        }
    }

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
                if (cursorPos > value.length) {
                    setCursor(value.length)
                }
                if (highlightPos > value.length) {
                    setHighlight(value.length)
                }

                val state = composableTheme.getState(
                    when {
                        !enabled -> TextureStates.DISABLED
                        focused -> TextureStates.CLICKED
                        else -> TextureStates.DEFAULT
                    },
                    theme.mode
                )

                if (composableTheme.isNinepatch) guiGraphics.ninePatchTexture(
                    x,
                    y,
                    node.width,
                    node.height,
                    state as NinePatchThemeState,
                ) else {
                    guiGraphics.blit(
                        (state as SimpleThemeState).texture,
                        x,
                        y,
                        state.width,
                        state.height,
                        state.u.toFloat(),
                        state.v.toFloat(),
                        state.uWidth,
                        state.vHeight,
                        state.textureSize.width,
                        state.textureSize.height,
                    )
                }

                val textColor = if (enabled) KColor.WHITE.argb else KColor.GRAY.argb
                val relativeCursorPos = cursorPos - displayPos
                val displayedText = font.plainSubstrByWidth(value.substring(displayPos), innerWidth)
                val isCursorWithinText = relativeCursorPos >= 0 && relativeCursorPos <= displayedText.length
                val showBlinkingCursor =
                    focused && ((Util.getMillis() - focusedTime) / 300L) % 2 == 0L && isCursorWithinText
                val startX = x + 4
                val startY = y + (node.height - innerPadding) / 2
                var textDrawX = startX
                val clampedHighlightPos = Mth.clamp(highlightPos - displayPos, 0, displayedText.length)

                if (displayedText.isNotEmpty()) {
                    val textBeforeCursor =
                        if (isCursorWithinText) displayedText.substring(0, relativeCursorPos) else displayedText

                    textDrawX = guiGraphics.drawString(
                        font,
                        FormattedCharSequence.forward(textBeforeCursor, Style.EMPTY),
                        startX,
                        startY,
                        textColor
                    )
                }

                val hasMoreTextAfterCursor = cursorPos < value.length || value.length >= maxLength
                var cursorX = textDrawX

                if (!isCursorWithinText) cursorX = if (relativeCursorPos > 0) startX + node.width else startX
                else if (hasMoreTextAfterCursor) {
                    cursorX = textDrawX - 1
                    textDrawX--
                }

                if (displayedText.isNotEmpty() && isCursorWithinText && relativeCursorPos < displayedText.length) {
                    val textAfterCursor = displayedText.substring(relativeCursorPos)
                    guiGraphics.drawString(
                        font,
                        FormattedCharSequence.forward(textAfterCursor, Style.EMPTY),
                        textDrawX,
                        startY,
                        textColor
                    )
                }

                if (placeholder != null && displayedText.isEmpty() && !focused) {
                    guiGraphics.drawString(font, placeholder, textDrawX, startY, KColor.GRAY.argb)
                }

                if (showBlinkingCursor) {
                    if (hasMoreTextAfterCursor) {
                        val fillYStart = startY - 1
                        val fillXEnd = cursorX + 1
                        val fillYEnd = startY + 1

                        guiGraphics.fill(
                            RenderType.guiOverlay(),
                            cursorX,
                            fillYStart,
                            fillXEnd,
                            fillYEnd + 9,
                            KColor(208, 208, 208).argb
                        )
                    } else {
                        guiGraphics.drawString(font, "_", cursorX, startY, textColor)
                    }
                }

                if (clampedHighlightPos != relativeCursorPos) {
                    val highlightX = startX + font.width(displayedText.substring(0, clampedHighlightPos))
                    val highlightYStart = startY - 1
                    val highlightXEnd = highlightX - 1
                    val highlightYEnd = startY + 10

                    var left = minOf(cursorX, highlightXEnd)
                    var right = maxOf(cursorX, highlightXEnd)
                    val top = minOf(highlightYStart, highlightYEnd)
                    val bottom = maxOf(highlightYStart, highlightYEnd)

                    val clampRight = x + node.width
                    if (right > clampRight) right = clampRight
                    if (left > clampRight) left = clampRight

                    guiGraphics.fill(RenderType.guiTextHighlight(), left, top, right, bottom, KColor.BLUE.argb)
                }

                super.render(node, x, y, guiGraphics, mouseX, mouseY, partialTick)
            }
        },
        modifier = Modifier.onKeyEvent { node, event ->
            if (!focused && !enabled) return@onKeyEvent

            var handled = true

            when (event.keyCode) {
                GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_DELETE -> if (!readOnly) removeText(-1)

                GLFW.GLFW_KEY_RIGHT -> if (Screen.hasControlDown()) {
                    setCursor(getWordPos(1))
                    if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                } else {
                    setCursor(Util.offsetByCodepoints(value, cursorPos, 1))
                    if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                }

                GLFW.GLFW_KEY_LEFT -> {
                    if (Screen.hasControlDown()) {
                        setCursor(getWordPos(-1))
                        if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                    } else {
                        setCursor(Util.offsetByCodepoints(value, cursorPos, -1))
                        if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                    }
                }

                GLFW.GLFW_KEY_HOME -> {
                    setCursor(0)
                    if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                }

                GLFW.GLFW_KEY_END -> {
                    setCursor(value.length)
                    if (!Screen.hasShiftDown()) setHighlight(cursorPos)
                }

                GLFW.GLFW_KEY_A if Screen.isSelectAll(event.keyCode) -> {
                    setCursor(value.length)
                    setHighlight(0)
                }

                GLFW.GLFW_KEY_C if Screen.isCopy(event.keyCode) -> Minecraft.getInstance().keyboardHandler.clipboard =
                    value.substring(
                        cursorPos.coerceAtMost(highlightPos),
                        cursorPos.coerceAtLeast(highlightPos)
                    )

                GLFW.GLFW_KEY_V if Screen.isPaste(event.keyCode) -> if (!readOnly) setText(Minecraft.getInstance().keyboardHandler.clipboard)

                GLFW.GLFW_KEY_X if Screen.isCut(event.keyCode) -> {
                    Minecraft.getInstance().keyboardHandler.clipboard =
                        value.substring(cursorPos.coerceAtMost(highlightPos), cursorPos.coerceAtLeast(highlightPos))
                    if (!readOnly) setText("")
                }

                GLFW.GLFW_KEY_TAB -> setText("    ")

                else -> handled = false
            }
            val mouseKey = InputConstants.getKey(event.keyCode, event.scanCode)

            if (!handled && !(mouseKey != InputConstants.UNKNOWN && (mouseKey == Minecraft.getInstance().options.keyInventory.key) && !Screen.hasShiftDown() && !Screen.hasControlDown() && !Screen.hasAltDown())) return@onKeyEvent

            event.consume((mouseKey != InputConstants.UNKNOWN && (mouseKey == Minecraft.getInstance().options.keyInventory.key) && !Screen.hasShiftDown() && !Screen.hasControlDown() && !Screen.hasAltDown()))
        }.onCharTyped { node, event ->
            if (enabled && focused && !readOnly && StringUtil.isAllowedChatCharacter(event.codePoint))
                setText(event.codePoint.toString())
            else return@onCharTyped

            event.consume()
        }.onPointerEvent(PointerEventType.PRESS) { node, event ->
            if (!enabled) return@onPointerEvent

            val relativeX = (floor(event.mouseX) - node.x).toInt()
            val visibleText = value.substring(displayPos)
            val newCursor = font.plainSubstrByWidth(visibleText, relativeX).length + displayPos

            setCursor(newCursor)

            if (!Screen.hasShiftDown()) setHighlight(cursorPos)

            focused = true
            focusedTime = Util.getMillis()

            event.consume()
        }.onPointerEvent(PointerEventType.GLOBAL_PRESS) { node, event ->
            if ((node as LayoutNode).isBounded(event.mouseX.toInt(), event.mouseY.toInt())) return@onPointerEvent

            focused = false

            event.consume()
        }.apply {
            if (!composableTheme.isNinepatch) with(composableTheme.states["default"]!!) {
                sizeIn(
                    minWidth = textureSize.width,
                    minHeight = textureSize.height
                )
            }
        } then modifier
    )
}