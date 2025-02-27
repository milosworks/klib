package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.*
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringUtil
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.extensions.ninePatchTexture
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.nodes.UINode
import kotlin.math.absoluteValue
import kotlin.math.floor

@Composable
fun TextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier,
	enabled: Boolean = true,
	readOnly: Boolean = false,
	singleLine: Boolean = false,
	maxLength: Int = 32,
	maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
	minLines: Int = 1,
	font: Font = Minecraft.getInstance().font,
) {
	var cursorPos: Int by remember { mutableStateOf(0) }
	var highlightPos: Int by remember { mutableStateOf(0) }
	var displayPos: Int by remember { mutableStateOf(0) }
	var focused: Boolean by remember { mutableStateOf(false) }

	var sizeModifier by remember { mutableStateOf(modifier.get<SizeModifier>()) }
	requireNotNull(sizeModifier) { "TextField requires a SizeModifier" }
	require(sizeModifier!!.constraints.minWidth == sizeModifier!!.constraints.maxWidth) { "TextField does not support content-sizing on width" }

	val texture: ResourceLocation by remember {
		mutableStateOf(
			modifier.firstOrNull<TextureModifier>()?.texture ?: KLib["text_field"]
		)
	}

	val innerPadding = 8
	val innerWidth = sizeModifier!!.constraints.maxWidth - innerPadding

	fun scrollTo(pos: Int) {
		displayPos = displayPos.coerceAtMost(value.length)
		val visibleText = font.plainSubstrByWidth(value.substring(displayPos), innerWidth)
		val visibleEnd = visibleText.length - displayPos

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
			if (str.toCharArray()[availableSpace - 1].isHighSurrogate()) availableSpace--

			str = str.substring(0, availableSpace)
		}

		val newStr = buildString {
			append(value)
			replace(start, end, str)
		}
		onValueChange(newStr)

		setCursor(start + value.length)
		setHighlight(cursorPos)
	}

	fun getWordPos(num: Int, pos: Int = cursorPos, skipSpaces: Boolean = true): Int {
		var currentPosition = pos
		val isMovingBackward = num < 0
		val absNumWords = num.absoluteValue

		repeat(absNumWords) {
			if (isMovingBackward) {
				// Move backward
				while (currentPosition > 0 && skipSpaces && value[currentPosition - 1] == ' ') {
					currentPosition--
				}
				while (currentPosition > 0 && value[currentPosition - 1] != ' ') {
					currentPosition--
				}
			} else {
				// Move forward
				currentPosition = value.indexOf(' ', currentPosition)

				if (currentPosition == -1) {
					currentPosition = value.length // End of string, return the length
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
		onValueChange(newValue)

		setCursor(start)
	}

	fun removeChars(count: Int) = removeCharsToPos(Util.offsetByCodepoints(value, cursorPos, count))

	fun removeWords(count: Int) {
		if (value.isEmpty()) return

		if (highlightPos != cursorPos) setText("")
		else removeCharsToPos(getWordPos(count))
	}

	fun removeText(count: Int) = if (Screen.hasControlDown()) removeWords(count) else removeChars(count)

	LaunchedEffect(value) {
		// Every change it runs
	}

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
				guiGraphics.ninePatchTexture(x, y, node.width, node.height, texture.withSuffix("/default"))
			}
		},
		modifier = Modifier.onKeyEvent { node, keyCode, scanCode, modifiers ->
			if (!focused && !enabled) return@onKeyEvent false

			when (keyCode) {
				259, 261 -> if (!readOnly) removeText(-1)

				262 -> if (Screen.hasControlDown()) {
					setCursor(getWordPos(1))
					if (Screen.hasShiftDown()) setHighlight(cursorPos)
				} else {
					setCursor(1)
					if (Screen.hasShiftDown()) setHighlight(cursorPos)
				}

				263 -> if (Screen.hasControlDown()) {
					setCursor(getWordPos(-1))
					if (Screen.hasShiftDown()) setHighlight(cursorPos)
				} else {
					setCursor(-1)
					if (Screen.hasShiftDown()) setHighlight(cursorPos)
				}

				268 -> {
					setCursor(0)
					if (!Screen.hasShiftDown()) setHighlight(cursorPos)
				}

				269 -> {
					setCursor(value.length)
					if (!Screen.hasShiftDown()) setHighlight(cursorPos)
				}

				65 if Screen.isSelectAll(keyCode) -> {
					setCursor(value.length)
					setHighlight(0)
				}

				67 if Screen.isCopy(keyCode) -> Minecraft.getInstance().keyboardHandler.clipboard = value.substring(
					cursorPos.coerceAtMost(highlightPos),
					cursorPos.coerceAtLeast(highlightPos)
				)

				86 if Screen.isPaste(keyCode) -> if (!readOnly) setText(Minecraft.getInstance().keyboardHandler.clipboard)

				88 if Screen.isCut(keyCode) -> {
					Minecraft.getInstance().keyboardHandler.clipboard =
						value.substring(cursorPos.coerceAtMost(highlightPos), cursorPos.coerceAtLeast(highlightPos))
					if (!readOnly) setText("")
				}

				else -> return@onKeyEvent false
			}

			true
		}.onCharTyped { node, codePoint, modifiers ->
			if (enabled && focused && !readOnly && StringUtil.isAllowedChatCharacter(codePoint))
				setText(codePoint.toString())
			else return@onCharTyped false

			true
		}.onPointerEvent(PointerEventType.PRESS) { node, x, y ->
			val relativeX = (floor(x) - node.x).toInt()
			val visibleText = value.substring(displayPos)
			val newCursor = font.plainSubstrByWidth(visibleText, relativeX).length + displayPos

			setCursor(newCursor)

			if (!Screen.hasShiftDown()) setHighlight(cursorPos)

			true
		} then modifier
	)
}