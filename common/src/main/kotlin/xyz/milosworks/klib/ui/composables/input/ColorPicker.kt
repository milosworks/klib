package xyz.milosworks.klib.ui.composables.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.measure.MeasurePolicy
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEvent
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onDrag
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent
import xyz.milosworks.klib.ui.utils.HsvColor
import xyz.milosworks.klib.ui.utils.KColor
import xyz.milosworks.klib.ui.utils.extensions.drawRectOutline
import xyz.milosworks.klib.ui.utils.extensions.fillGradient
import kotlin.math.max

@Composable
private fun SaturationValueArea(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChanged: (saturation: Float, value: Float) -> Unit
) {
    val onEvent = { uiNode: LayoutNode, event: PointerEvent ->
        val newSaturation =
            ((event.mouseX - uiNode.absoluteCoords.x) / uiNode.width).toFloat()
                .coerceIn(0f, 1f)
        val newValue =
            (1f - ((event.mouseY - uiNode.absoluteCoords.y) / uiNode.height).toFloat()).coerceIn(
                0f,
                1f
            )
        onSaturationValueChanged(newSaturation, newValue)
        event.consume()
    }

    Layout(
        measurePolicy = { _, _, constraints ->
            MeasureResult(
                constraints.minWidth,
                constraints.minHeight
            ) {}
        },
        renderer = object : Renderer {
            override fun render(
                uiNode: UINode, x: Int, y: Int, guiGraphics: GuiGraphics,
                mouseX: Int, mouseY: Int, partialTick: Float
            ) {
                guiGraphics.fillGradient(
                    x,
                    y,
                    uiNode.width,
                    uiNode.height,
                    // Top-Left
                    KColor.ofHsv(hue, 0f, 1f).argb,
                    // Top-Right
                    KColor.ofHsv(hue, 1f, 1f).argb,
                    // Bottom-Left
                    KColor.ofHsv(hue, 0f, 0f).argb,
                    // Bottom-Right
                    KColor.ofHsv(hue, 1f, 0f).argb
                )

                guiGraphics.drawRectOutline(
                    x + (saturation * uiNode.width).toInt() - 2,
                    y + ((1 - value) * uiNode.height).toInt() - 2,
                    4,
                    4,
                    KColor.WHITE.argb
                )
            }
        },
        modifier = modifier
            .onPointerEvent(PointerEventType.PRESS, onEvent)
            .onDrag(onDragEvent = onEvent)
    )
}

@Composable
private fun HueBar(
    modifier: Modifier = Modifier,
    hue: Float,
    onHueChanged: (hue: Float) -> Unit
) {
    val onEvent = { node: LayoutNode, event: PointerEvent ->
        val newHue =
            (1f - ((event.mouseY - node.absoluteCoords.y) / node.height).toFloat()).coerceIn(
                0f,
                1f
            )
        onHueChanged(newHue)
        event.consume()
    }

    Layout(
        measurePolicy = { _, _, constraints ->
            MeasureResult(
                constraints.minWidth,
                constraints.minHeight
            ) {}
        },
        renderer = object : Renderer {
            override fun render(
                uiNode: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                for (j in 0 until uiNode.height) {
                    val currentHue = 1f - (j.toFloat() / uiNode.height)
                    guiGraphics.fill(
                        x,
                        y + j,
                        x + uiNode.width,
                        y + j + 1,
                        KColor.ofHsv(currentHue, 1f, 1f).argb
                    )
                }

                guiGraphics.drawRectOutline(
                    x - 1,
                    y + ((1 - hue) * uiNode.height).toInt() - 1,
                    uiNode.width + 2,
                    3,
                    KColor.WHITE.argb
                )
            }
        },
        modifier = modifier
            .onPointerEvent(PointerEventType.PRESS, onEvent)
            .onDrag(onDragEvent = onEvent)
    )
}

@Composable
private fun AlphaBar(
    modifier: Modifier = Modifier,
    color: HsvColor,
    onAlphaChanged: (alpha: Float) -> Unit
) {
    val onEvent = { node: LayoutNode, event: PointerEvent ->
        val newAlpha =
            ((event.mouseX - node.absoluteCoords.x) / node.width).toFloat().coerceIn(0f, 1f)
        onAlphaChanged(newAlpha)
        event.consume()
    }

    Layout(
        measurePolicy = { _, _, constraints ->
            MeasureResult(
                constraints.minWidth,
                constraints.minHeight
            ) {}
        },
        renderer = object : Renderer {
            override fun render(
                uiNode: UINode,
                x: Int,
                y: Int,
                guiGraphics: GuiGraphics,
                mouseX: Int,
                mouseY: Int,
                partialTick: Float
            ) {
                val checkerSize = 4
                for (cx in 0 until uiNode.width step checkerSize) {
                    for (cy in 0 until uiNode.height step checkerSize) {
                        val c =
                            if ((cx / checkerSize + cy / checkerSize) % 2 == 0) KColor.WHITE.rgb else KColor.LIGHT_GRAY.rgb
                        guiGraphics.fill(
                            x + cx,
                            y + cy,
                            x + cx + checkerSize,
                            y + cy + checkerSize,
                            c
                        )
                    }
                }

                val opaqueColor = color.copy(alpha = 1f).toKColor().argb
                // we make the transparent color have a hue so it's not just black
                val transparentColor = color.copy(alpha = 0f).toKColor().argb

                guiGraphics.fillGradient(
                    x,
                    y,
                    uiNode.width,
                    uiNode.height,
                    // Top-Left
                    transparentColor,
                    // Top-Right
                    opaqueColor,
                    // Bottom-Left
                    transparentColor,
                    // Bottom-Right
                    opaqueColor
                )

                guiGraphics.drawRectOutline(
                    x + (color.alpha * uiNode.width).toInt() - 1,
                    y - 1,
                    3,
                    uiNode.height + 2,
                    KColor.WHITE.argb
                )
            }
        },
        modifier = modifier
            .onPointerEvent(PointerEventType.PRESS, onEvent)
            .onDrag(onDragEvent = onEvent)
    )
}

/**
 * A Color Picker composable that allows for selection of a color via HSV and Alpha sliders.
 *
 * This composable is fully controlled. It displays the color provided in the `color` parameter
 * and reports any user interactions via the `onColorChanged` callback. The caller is responsible
 * for creating and remembering the state.
 *
 * @param color The current HsvColor to display.
 * @param showAlphaBar Whether to show the alpha slider.
 * @param alphaBarHeight The height of the alpha slider.
 * @param hueBarWidth The width of the hue slider.
 * @param barPadding The padding between the main color area and the sliders.
 * @param modifier Modifiers to apply to the color picker container. A size modifier (e.g., Modifier.size(width, height))
 * is required for the picker to be rendered correctly.
 * @param onColorChanged A callback that is triggered when the user interacts with the picker. It provides the new HsvColor value.
 */
@Composable
fun ColorPicker(
    color: HsvColor,
    showAlphaBar: Boolean = true,
    alphaBarHeight: Int = 12,
    hueBarWidth: Int = 16,
    barPadding: Int = 8,
    modifier: Modifier = Modifier,
    onColorChanged: (HsvColor) -> Unit,
) {
    val updatedOnColorChanged by rememberUpdatedState(onColorChanged)

    val measurePolicy = remember(showAlphaBar, alphaBarHeight, hueBarWidth, barPadding) {
        MeasurePolicy { _, measurables, constraints ->
            if (showAlphaBar) {
                check(measurables.size == 3) { "ColorPicker expected 3 children when showAlphaBar is true" }
                val (svMeasurable, alphaMeasurable, hueMeasurable) = measurables

                val finalHueBarWidth = hueBarWidth
                val finalAlphaBarHeight = alphaBarHeight
                val padding = barPadding

                val svAreaWidth = max(0, constraints.maxWidth - finalHueBarWidth - padding)
                val svAreaHeight = max(0, constraints.maxHeight - finalAlphaBarHeight - padding)

                val svPlaceable = svMeasurable.measure(
                    Constraints(svAreaWidth, svAreaWidth, svAreaHeight, svAreaHeight)
                )
                val alphaPlaceable = alphaMeasurable.measure(
                    Constraints(svAreaWidth, svAreaWidth, finalAlphaBarHeight, finalAlphaBarHeight)
                )
                val huePlaceable = hueMeasurable.measure(
                    Constraints(
                        finalHueBarWidth,
                        finalHueBarWidth,
                        constraints.maxHeight,
                        constraints.maxHeight
                    )
                )

                MeasureResult(constraints.maxWidth, constraints.maxHeight) {
                    svPlaceable.placeAt(0, 0)
                    alphaPlaceable.placeAt(0, svPlaceable.height + padding)
                    huePlaceable.placeAt(svPlaceable.width + padding, 0)
                }
            } else {
                check(measurables.size == 2) { "ColorPicker expected 2 children when showAlphaBar is false" }
                val (svMeasurable, hueMeasurable) = measurables

                val svAreaWidth = max(0, constraints.maxWidth - hueBarWidth - barPadding)
                val svAreaHeight = constraints.maxHeight

                val svPlaceable = svMeasurable.measure(
                    Constraints(svAreaWidth, svAreaWidth, svAreaHeight, svAreaHeight)
                )
                val huePlaceable = hueMeasurable.measure(
                    Constraints(
                        hueBarWidth,
                        hueBarWidth,
                        constraints.maxHeight,
                        constraints.maxHeight
                    )
                )

                MeasureResult(constraints.maxWidth, constraints.maxHeight) {
                    svPlaceable.placeAt(0, 0)
                    huePlaceable.placeAt(svPlaceable.width + barPadding, 0)
                }
            }
        }
    }

    Layout(
        measurePolicy = measurePolicy,
        modifier = modifier
    ) {
        SaturationValueArea(
            hue = color.hue,
            saturation = color.saturation,
            value = color.value,
            onSaturationValueChanged = { saturation, value ->
                updatedOnColorChanged(color.copy(saturation = saturation, value = value))
            }
        )
        if (showAlphaBar) {
            AlphaBar(
                color = color,
                onAlphaChanged = { alpha ->
                    updatedOnColorChanged(color.copy(alpha = alpha))
                }
            )
        }
        HueBar(
            hue = color.hue,
            onHueChanged = { hue ->
                updatedOnColorChanged(color.copy(hue = hue))
            }
        )
    }
}