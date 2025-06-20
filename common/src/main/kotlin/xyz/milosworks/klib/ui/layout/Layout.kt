package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.base.ui1.nodes.UINodeApplier
import xyz.milosworks.klib.ui.layout.measure.MeasurePolicy
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.modifiers.core.Modifier

/**
 * A fundamental composable that is used to create custom layouts by defining the measurement
 * and rendering policies. It serves as a base for other composables, allowing developers to
 * define their own measure and render logic while managing child content.
 *
 * This composable is commonly used by other composables (e.g., `Text`, `Column`, `Row`, `Spacer`)
 * to set up their specific layout behaviors.
 *
 * To create your own composable, you need to define a [MeasurePolicy] that dictates how the
 * content should be measured and a [Renderer] that defines how it should be drawn.
 *
 * Example usage:
 * ```
 * @Composable
 * fun MyCustomComposable(modifier: Modifier = Modifier) {
 *     Layout(
 *         measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
 *         renderer = DefaultRenderer(),
 *         modifier = modifier,
 *     )
 * }
 * ```
 *
 * @param measurePolicy The policy that defines how to measure and place children.
 * @param renderer The renderer to draw the content. Default is [EmptyRenderer], which renders nothing.
 * @param modifier The modifier to be applied to the layout. Default is [Modifier].
 * @param content The composable content to be rendered. Default is an empty composable.
 */
@Composable
inline fun Layout(
    measurePolicy: MeasurePolicy,
    renderer: Renderer = EmptyRenderer,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    ComposeNode<UINode, UINodeApplier>(
        factory = UINode.Companion.Constructor(Thread.currentThread().stackTrace[1].methodName),
        update = {
            set(measurePolicy) { this.measurePolicy = it }
            set(renderer) { this.renderer = it }
            set(modifier) { this.modifier = it }
        },
        content = content,
    )
}