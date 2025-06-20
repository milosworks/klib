package xyz.milosworks.klib.ui.components.basic

import androidx.compose.runtime.Composable
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.layout.fillMaxSize

/**
 * A layout composable that takes up empty space.
 *
 * `Spacer` is useful for creating flexible gaps between UI elements
 * without adding visual components.
 */
@Composable
fun Spacer(modifier: Modifier = Modifier) {
    Layout(
        measurePolicy = { _, _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
        renderer = DefaultRenderer(),
        modifier = modifier.fillMaxSize(),
    )
}