package xyz.milosworks.klib.ui.composables.input.textfield

import androidx.compose.runtime.Immutable
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a range of text, defined by a start and end cursor position.
 * The range is inclusive of the start index and exclusive of the end index.
 *
 * @param start The starting index of the range (inclusive).
 * @param end The ending index of the range (exclusive). If not provided, it defaults to the start,
 *            representing a collapsed range (a cursor).
 */
@Immutable
data class TextRange(val start: Int, val end: Int = start) {
    /** True if the range is collapsed, meaning start and end are the same. */
    val isCollapsed: Boolean get() = start == end

    /** The length of the text range. */
    val length: Int get() = max(start, end) - min(start, end)

    /** The minimum of start and end, representing the inclusive start of the range. */
    val min: Int get() = minOf(start, end)

    /** The maximum of start and end, representing the exclusive end of the range. */
    val max: Int get() = maxOf(start, end)

    companion object {
        /** A collapsed text range at the zero index. */
        val Zero = TextRange(0)
    }

    override fun toString(): String {
        return "TextRange(start=$start, end=$end)"
    }
}

/**
 * An immutable data class holding the state for a text field, including the text itself,
 * the selection range, and an optional composition range for input methods.
 *
 * @param text The current text content.
 * @param selection The current selection range or cursor position.
 * @param composition The range of text currently being composed by an IME.
 */
@Immutable
data class TextFieldValue(
    val text: String = "",
    val selection: TextRange = TextRange(text.length),
    val composition: TextRange? = null
) {
    /** The text currently selected by the user. */
    val selectedText: String
        get() = text.substring(selection.min, selection.max)

    override fun toString(): String {
        return "TextFieldValue(text='$text', selection=$selection, composition=$composition)"
    }
}