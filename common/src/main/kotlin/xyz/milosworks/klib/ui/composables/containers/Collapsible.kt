package xyz.milosworks.klib.ui.composables.containers

import androidx.compose.runtime.*
import com.mojang.math.Axis
import kotlinx.coroutines.delay
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.composables.basic.Spacer
import xyz.milosworks.klib.ui.composables.basic.Text
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.containers.Column
import xyz.milosworks.klib.ui.layout.containers.Row
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.layout.primitive.Arrangement
import xyz.milosworks.klib.ui.modifiers.appearance.background
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.modifiers.layout.fillMaxHeight
import xyz.milosworks.klib.ui.modifiers.layout.size
import xyz.milosworks.klib.ui.modifiers.position.padding.padding
import xyz.milosworks.klib.ui.utils.KColor
import kotlin.math.abs

/**
 * A container that can be expanded or collapsed to show or hide its content.
 *
 * @param title The text displayed in the header of the collapsible section.
 * @param modifier The modifier to be applied to the container.
 * @param initiallyExpanded Whether the container should be expanded by default.
 * @param onToggled A callback that is invoked when the expanded state changes.
 * @param content The composable content to be displayed when the container is expanded.
 */
@Composable
fun Collapsible(
    title: Component,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    onToggled: (isExpanded: Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4)) {
        Row(
            modifier = Modifier.onPointerEvent<UINode>(PointerEventType.PRESS) { _, e ->
                expanded = !expanded
                onToggled(expanded)
                e.consume()
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5)
        ) {
            Spinner(isExpanded = expanded)
            Text(text = title.copy().withStyle(Style.EMPTY.withUnderlined(true)))
        }

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(5)) {
                Spacer(
                    modifier = Modifier
                        .padding(left = 5)
                        .size(width = 1, height = 0)
                        .fillMaxHeight()
                        .background(KColor.GRAY)
                )

                Box(modifier = Modifier.padding(left = 5)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun Spinner(isExpanded: Boolean) {
    var rotation by remember { mutableStateOf(if (isExpanded) 90f else 0f) }
    val targetRotation = if (isExpanded) 90f else 0f

    LaunchedEffect(targetRotation) {
        while (abs(targetRotation - rotation) > 0.1f) {
            rotation += (targetRotation - rotation) * 0.25f
            // this is roughly 60fps
            delay(16)
        }
        rotation = targetRotation
    }

    Layout(
        measurePolicy = { _, _, _ ->
            val size = 8
            MeasureResult(size, size) {}
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
                guiGraphics.pose().pushPose()

                guiGraphics.pose().translate(x + node.width / 2f, y + node.height / 2f, 0f)
                guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation))

                guiGraphics.pose().translate(-(x + node.width / 2f), -(y + node.height / 2f), 0f)

                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    ">",
                    x + 1,
                    y,
                    KColor.WHITE.argb
                )

                guiGraphics.pose().popPose()
            }
        }
    )
}