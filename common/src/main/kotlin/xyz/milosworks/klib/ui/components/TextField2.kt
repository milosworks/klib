package xyz.milosworks.klib.ui.components

//@Composable
//fun TextField(
//	value: String,
//	onValueChange: (String) -> Unit,
//	modifier: Modifier = Modifier,
//	enabled: Boolean = true,
//	readOnly: Boolean = false,
//	singleLine: Boolean = false,
//	maxLength: Int = 32,
//	font: Font = Minecraft.getInstance().font,
//) {
//	// States for focus and text editing
//	var focused by remember { mutableStateOf(false) }
//	var cursorPos by remember { mutableStateOf(value.length) }
//	var highlightPos by remember { mutableStateOf(cursorPos) }
//	var displayPos by remember { mutableStateOf(0) }
//
//	// We assume the SizeModifier is provided; text field requires a fixed width.
//	val sizeModifier = modifier.getOrNull<xyz.milosworks.klib.ui.modifiers.SizeModifier>()
//	requireNotNull(sizeModifier) { "TextField requires a SizeModifier" }
//	// Use an 8-pixel horizontal inset (4 pixels per side) like Minecraft’s EditBox.
//	val horizontalPadding = 8
//	val innerWidth = sizeModifier.constraints.maxWidth - horizontalPadding
//
//	// Helpers to keep the visible part of the text in sync.
//	fun scrollTo(pos: Int) {
//		// Ensure displayPos is within value bounds.
//		displayPos = displayPos.coerceIn(0, value.length)
//		val visibleText = font.plainSubstrByWidth(value.substring(displayPos), innerWidth)
//		val visibleLength = visibleText.length
//		when {
//			pos < displayPos -> displayPos = pos
//			pos > displayPos + visibleLength -> displayPos = pos - visibleLength
//		}
//		displayPos = displayPos.coerceIn(0, value.length)
//	}
//
//	fun setCursor(pos: Int) {
//		cursorPos = pos.coerceIn(0, value.length)
//		scrollTo(cursorPos)
//	}
//
//	fun setHighlight(pos: Int) {
//		highlightPos = pos.coerceIn(0, value.length)
//		scrollTo(highlightPos)
//	}
//
//	fun moveCursor(delta: Int, select: Boolean) {
//		val newPos = Util.offsetByCodepoints(value, cursorPos, delta)
//		setCursor(newPos)
//		if (!select) {
//			setHighlight(cursorPos)
//		}
//	}
//
//	fun insertText(text: String) {
//		val start = cursorPos.coerceAtMost(highlightPos)
//		val end = cursorPos.coerceAtLeast(highlightPos)
//		// Calculate available space.
//		val available = maxLength - (value.length - (end - start))
//		if (available <= 0) return
//		var filtered = StringUtil.filterText(text)
//		if (filtered.length > available) {
//			// Avoid breaking surrogate pairs.
//			if (filtered[available - 1].isHighSurrogate()) {
//				filtered = filtered.substring(0, available - 1)
//			} else {
//				filtered = filtered.substring(0, available)
//			}
//		}
//		val newValue = value.substring(0, start) + filtered + value.substring(end)
//		onValueChange(newValue)
//		val newCursor = start + filtered.length
//		setCursor(newCursor)
//		setHighlight(newCursor)
//	}
//
//	fun deleteText(delta: Int) {
//		// If selection exists, simply remove it.
//		if (cursorPos != highlightPos) {
//			val start = cursorPos.coerceAtMost(highlightPos)
//			val end = cursorPos.coerceAtLeast(highlightPos)
//			onValueChange(value.removeRange(start, end))
//			setCursor(start)
//			setHighlight(start)
//		} else {
//			val pos = if (delta < 0) (cursorPos + delta).coerceAtLeast(0) else cursorPos
//			if (pos == cursorPos) return
//			onValueChange(value.removeRange(pos, cursorPos))
//			setCursor(pos)
//			setHighlight(pos)
//		}
//	}
//
//	fun getWordPos(num: Int, pos: Int): Int {
//		var currentPos = pos
//		if (num < 0) {
//			repeat(num.absoluteValue) {
//				// Skip any preceding spaces.
//				while (currentPos > 0 && value[currentPos - 1] == ' ') {
//					currentPos--
//				}
//				// Skip until reaching a space.
//				while (currentPos > 0 && value[currentPos - 1] != ' ') {
//					currentPos--
//				}
//			}
//		} else {
//			repeat(num) {
//				val nextSpace = value.indexOf(' ', currentPos)
//				currentPos = if (nextSpace == -1) value.length else nextSpace + 1
//			}
//		}
//		return currentPos
//	}
//
//	// Layout with our custom measure and renderer.
//	Layout(
//		measurePolicy = { _, constraints ->
//			// Use the provided width and a fixed height (e.g. font height plus padding).
//			val width = constraints.maxWidth
//			val height = font.lineHeight + 8
//			MeasureResult(width, height) {}
//		},
//		renderer = object : Renderer {
//			override fun render(
//				node: UINode,
//				x: Int,
//				y: Int,
//				guiGraphics: GuiGraphics,
//				mouseX: Int,
//				mouseY: Int,
//				partialTick: Float
//			) {
//				// Draw the text field background. (You may replace this with your texture drawing.)
//				guiGraphics.fill(
//					x,
//					y,
//					x + sizeModifier.constraints.maxWidth,
//					y + font.lineHeight + 8,
//					0xFF555555.toInt()
//				)
//				// Compute text drawing position.
//				val textX = x + 4
//				val textY = y + 4
//				// Draw visible text.
//				val visibleText = font.plainSubstrByWidth(value.substring(displayPos), innerWidth)
//				val textColor = if (enabled) 14737632 else 7368816
//				val drawnEnd = font.drawString(guiGraphics, visibleText, textX, textY, textColor)
//				// If focused, draw the blinking cursor.
//				if (focused) {
//					// For simplicity, the cursor is always drawn here.
//					val visibleCursorPos = cursorPos - displayPos
//					val cursorX = textX + font.width(value.substring(displayPos, cursorPos))
//					guiGraphics.fill(cursorX, textY, cursorX + 1, textY + font.lineHeight, -3092272)
//				}
//				// Draw highlight if a selection exists.
//				if (cursorPos != highlightPos) {
//					val start = cursorPos.coerceAtMost(highlightPos)
//					val end = cursorPos.coerceAtLeast(highlightPos)
//					val highlightStartX = textX + font.width(value.substring(displayPos, start))
//					val highlightEndX = textX + font.width(value.substring(displayPos, end))
//					guiGraphics.fill(highlightStartX, textY, highlightEndX, textY + font.lineHeight, 0x663399FF)
//				}
//			}
//		},
//		modifier = Modifier
//			// Handle key events.
//			.onKeyEvent { node, keyCode, scanCode, modifiers ->
//				if (!enabled) return@onKeyEvent false
//				when (keyCode) {
//					259 -> { // Backspace
//						if (!readOnly) deleteText(-1)
//						true
//					}
//
//					261 -> { // Delete
//						if (!readOnly) deleteText(1)
//						true
//					}
//
//					262 -> { // Right arrow
//						if (Screen.hasControlDown()) {
//							setCursor(getWordPos(1, cursorPos))
//						} else {
//							moveCursor(1, Screen.hasShiftDown())
//						}
//						true
//					}
//
//					263 -> { // Left arrow
//						if (Screen.hasControlDown()) {
//							setCursor(getWordPos(-1, cursorPos))
//						} else {
//							moveCursor(-1, Screen.hasShiftDown())
//						}
//						true
//					}
//
//					268 -> { // Home
//						setCursor(0)
//						if (!Screen.hasShiftDown()) setHighlight(cursorPos)
//						true
//					}
//
//					269 -> { // End
//						setCursor(value.length)
//						if (!Screen.hasShiftDown()) setHighlight(cursorPos)
//						true
//					}
//					// Handle select-all (Ctrl+A) if needed.
//					65 -> {
//						if (Screen.isSelectAll(keyCode)) {
//							setCursor(value.length)
//							setHighlight(0)
//							true
//						} else false
//					}
//					// Copy, paste, and cut.
//					67 -> {
//						if (Screen.isCopy(keyCode)) {
//							Minecraft.getInstance().keyboardHandler.clipboard = value.substring(
//								cursorPos.coerceAtMost(highlightPos),
//								cursorPos.coerceAtLeast(highlightPos)
//							)
//							true
//						} else false
//					}
//
//					86 -> {
//						if (Screen.isPaste(keyCode) && !readOnly) {
//							insertText(Minecraft.getInstance().keyboardHandler.clipboard)
//							true
//						} else false
//					}
//
//					88 -> {
//						if (Screen.isCut(keyCode)) {
//							Minecraft.getInstance().keyboardHandler.clipboard = value.substring(
//								cursorPos.coerceAtMost(highlightPos),
//								cursorPos.coerceAtLeast(highlightPos)
//							)
//							if (!readOnly) deleteText(0)
//							true
//						} else false
//					}
//
//					else -> false
//				}
//			}
//			// Handle character input.
//			.onCharTyped { node, codePoint, modifiers ->
//				if (!readOnly && StringUtil.isAllowedChatCharacter(codePoint)) {
//					insertText(codePoint.toString())
//					true
//				} else false
//			}
//			// Handle mouse clicks to update cursor position.
//			.onPointerEvent(PointerEventType.PRESS) { node, pointerX, pointerY ->
//				val relativeX = (floor(pointerX) - node.x - 4).toInt()
//				val visibleText = value.substring(displayPos)
//				val newCursor = font.plainSubstrByWidth(visibleText, relativeX).length + displayPos
//				setCursor(newCursor)
//				if (!Screen.hasShiftDown()) setHighlight(cursorPos)
//				true
//			}
//	)
//
//	// Optionally, you might want to update focus state via a modifier or external state.
//	// For example:
//	LaunchedEffect(Unit) {
//		// When the node is clicked or otherwise focused, set focused = true.
//		// (Implement your focus management logic here.)
//	}
//}
