package xyz.milosworks.klib.ui.extensions

import net.minecraft.client.gui.screens.Screen
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.modifiers.input.*

internal fun <T : InputEvent> Screen.processInputEvent(
    node: LayoutNode,
    event: T,
    condition: (LayoutNode) -> Boolean = { true },
    process: (LayoutNode, T) -> Unit
) {
    for (child in node.children.asReversed()) {
        if (!event.isConsumed) processInputEvent(child, event, condition, process)
        else break
    }
    if (!event.isConsumed && condition(node)) process(node, event)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Screen.processPointerEvent(
    node: LayoutNode,
    mouseX: Double,
    mouseY: Double,
    eventType: PointerEventType,
    global: Boolean = false,
    noinline condition: (LayoutNode) -> Boolean = { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
): PointerEvent {
    val event = PointerEvent(eventType, mouseX, mouseY)

    processInputEvent(node, event, if (global) { _ -> true } else condition) { currentNode, currentEvent ->
        currentNode.modifier.foldIn(Unit) { acc, el ->
            if (el is OnPointerEventModifier && el.eventType == eventType && (global || !currentEvent.isConsumed))
                el.onEvent(currentNode, event)
        }
    }
    return event
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Screen.processGlobalPointerEvent(
    node: LayoutNode,
    mouseX: Double,
    mouseY: Double,
    eventType: PointerEventType,
) {
    val event = PointerEvent(PointerEventType.GLOBAL_PRESS, mouseX, mouseY)

    processInputEvent(node, event) { currentNode, currentEvent ->
        currentNode.modifier.foldIn(Unit) { acc, el ->
            if (el is OnPointerEventModifier && el.eventType == eventType)
                el.onEvent(currentNode, event)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Screen.processKeyEvent(
    node: LayoutNode,
    keyCode: Int,
    scanCode: Int,
    modifiers: Int
): KeyEvent {
    val event = KeyEvent(keyCode, scanCode, modifiers)

    processInputEvent(node, event) { currentNode, currentEvent ->
        currentNode.modifier.foldIn(Unit) { acc, el ->
            if (el is OnKeyEventModifier && !currentEvent.isConsumed) el.onEvent(currentNode, currentEvent)
        }
    }

    return event
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Screen.processCharEvent(
    node: LayoutNode,
    codePoint: Char,
    modifiers: Int
): CharEvent {
    val event = CharEvent(codePoint, modifiers)

    processInputEvent(node, event) { currentNode, currentEvent ->
        currentNode.modifier.foldIn(Unit) { acc, el ->
            if (el is OnCharTypedModifier && !currentEvent.isConsumed) el.onEvent(currentNode, currentEvent)
        }
    }

    return event
}