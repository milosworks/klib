package xyz.milosworks.klib.ui.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import xyz.milosworks.klib.ui.base.UINodeApplier
import xyz.milosworks.klib.ui.layout.LayoutNode
import java.util.*

/**
 * Represents a single, self-contained UI layer with its own root node and composition.
 * This allows for an independent layout and state management, crucial for popups and modals.
 */
class Layer(
    val id: UUID = UUID.randomUUID(),
    parentComposition: CompositionContext,
    content: @Composable () -> Unit
) {
    val rootNode = LayoutNode()
    val composition = Composition(UINodeApplier(rootNode), parentComposition)

    init {
        composition.setContent(content)
    }

    /**
     * Disposes the composition associated with this layer, releasing its resources.
     */
    fun dispose() {
        composition.dispose()
    }
}