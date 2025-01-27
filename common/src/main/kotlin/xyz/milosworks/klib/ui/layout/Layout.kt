package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.nodes.UINode
import xyz.milosworks.klib.ui.nodes.UINodeApplier

@Composable
inline fun Layout(
	measurePolicy: MeasurePolicy,
	renderer: Renderer = EmptyRenderer,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit = {}
) {
	ComposeNode<UINode, UINodeApplier>(
		factory = UINode.Constructor,
		update = {
			set(measurePolicy) { this.measurePolicy = it }
			set(renderer) { this.renderer = it }
			set(modifier) { this.modifier = it }
		},
		content = content,
	)
}

