package xyz.milosworks.klib.ui.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.fillMaxSize
import java.util.*

/**
 * Provides the current `LayerStackManager` to the composition tree.
 * Essential for allowing nested composables to create new layers.
 */
val LocalLayerManager = compositionLocalOf<LayerStackManager> {
    error("No LayerManager provided. Are you inside a ComposeScreen?")
}

/**
 * Defines the context for a modal's content, providing a way for the modal
 * to dismiss itself.
 */
interface ModalScope {
    /**
     * Dismisses the modal layer.
     */
    fun dismiss()
}

/**
 * Manages a stack of UI layers, handling the creation, ordering and disposal of popups and overlays.
 *
 * @param parentComposition The composition context from the parent screen, required to create new layers.
 */
class LayerStackManager(
    private val parentComposition: CompositionContext,
) {
    val layers = mutableStateListOf<Layer>()

    /**
     * Pushes a new, generic layer onto the stack.
     * The content lambda receives a `dismiss` function that it can call to remove itself.
     * This is useful for creating persistent overlays or custom layer types.
     *
     * @param layerContent The composable content for the layer, which receives a dismiss function.
     * @return A dismiss handle that the caller can use to imperatively remove the layer.
     */
    fun push(layerContent: @Composable (dismiss: () -> Unit) -> Unit): () -> Unit {
        val layerId = UUID.randomUUID()
        val layer = Layer(
            id = layerId,
            parentComposition = parentComposition,
            content = {
                layerContent { popById(layerId) }
            }
        )

        layers.add(layer)
        return { popById(layerId) }
    }

    /**
     * Pushes a new modal layer onto the stack with a convenient scope for dismissal.
     * Modals are opinionated, input-blocking layers ideal for dialogs and popups.
     *
     * @param alignment The alignment of the modal content within the screen. Defaults to `Alignment.Center`.
     * @param dismissOnClickOutside If true, a click outside the modal's content will trigger dismissal.
     * @param onDismissRequest An optional callback invoked when the modal is dismissed.
     * @param content The modal's UI content, a composable lambda with a `ModalScope` receiver.
     */
    fun modal(
        alignment: Alignment = Alignment.Center,
        dismissOnClickOutside: Boolean = true,
        onDismissRequest: () -> Unit = {},
        content: @Composable ModalScope.() -> Unit
    ) {
        push { dismiss ->
            val scope = object : ModalScope {
                override fun dismiss() {
                    dismiss()
                }
            }

            ModalLayout(
                alignment = alignment,
                dismissOnClickOutside = dismissOnClickOutside,
                onDismissRequest = {
                    onDismissRequest()
                    dismiss()
                },
                content = { scope.content() }
            )
        }
    }

    /**
     * Pops the topmost layer from the stack and disposes it.
     */
    fun pop() {
        layers.removeLastOrNull()?.dispose()
    }

    /**
     * Pops a specific layer identified by its ID from the stack.
     * @param id The unique ID of the layer to remove.
     */
    fun popById(id: UUID) {
        val layer = layers.find { it.id == id }
        if (layer != null) {
            layer.dispose()
            layers.remove(layer)
        }
    }

    /**
     * The topmost layer in the stack, which is the one that should receive input.
     */
    val top: Layer?
        get() = layers.lastOrNull()

    @Composable
    private fun ModalLayout(
        alignment: Alignment,
        onDismissRequest: () -> Unit,
        dismissOnClickOutside: Boolean,
        content: @Composable () -> Unit
    ) {
        var rootModifier = Modifier.fillMaxSize()
        if (dismissOnClickOutside) {
            rootModifier = rootModifier.onPointerEvent<UINode>(PointerEventType.PRESS) { _, event ->
                onDismissRequest()
                event.consume()
            }
        }

        Box(
            modifier = rootModifier,
            contentAlignment = alignment
        ) {
            Box(modifier = Modifier.onPointerEvent<UINode>(PointerEventType.PRESS) { _, event ->
                event.consume()
            }) {
                content()
            }
        }
    }
}