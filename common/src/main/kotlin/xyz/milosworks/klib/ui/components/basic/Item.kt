package xyz.milosworks.klib.ui.components.basic

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.Lighting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import org.joml.Matrix4f
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.input.onPointerEvent

val SCALING: Matrix4f = Matrix4f().scaling(16f, -16f, 16f)

@Composable
fun Item(
    item: ItemStack,
    showTooltip: Boolean = false,
    tooltips: List<TooltipComponent> = emptyList(),
    tooltipFromItem: Boolean = false,
    tooltipType: TooltipFlag? = null,
    tooltipContext: TooltipContext = TooltipContext.EMPTY,
    modifier: Modifier = Modifier
) {
    var hovered by remember { mutableStateOf(false) }
    val type =
        if (tooltipType == null && Minecraft.getInstance().options.advancedItemTooltips)
            TooltipFlag.ADVANCED
        else TooltipFlag.NORMAL

    val allTooltips = buildList {
        if (tooltipFromItem) {
            addAll(
                item.getTooltipLines(tooltipContext, null, type)
                    .map { ClientTooltipComponent.create(it.visualOrderText) }
            )
        }

        addAll(tooltips)
    }

    Layout(
        measurePolicy = { _, _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
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
                val usesBlockLight = Minecraft.getInstance().itemRenderer.getModel(item, null, null, 0).usesBlockLight()
                if (!usesBlockLight) {
                    Lighting.setupForFlatItems()
                }

                val stack = guiGraphics.pose()
                stack.pushPose()

                stack.translate(x.toFloat(), y.toFloat(), 100f)

                stack.scale(node.width / 16f, node.height / 16f, 1f)
                stack.translate(8.0, 8.0, 0.0)

                if (usesBlockLight) {
                    stack.mulPose(SCALING)
                } else {
                    stack.scale(16f, -16f, 16f)
                }

                Minecraft.getInstance().itemRenderer.renderStatic(
                    item, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, stack,
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    Minecraft.getInstance().level, 0
                )
                Minecraft.getInstance().renderBuffers().bufferSource().endBatch()

                stack.popPose()

                if (showTooltip) {
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y)
                }

                if (!usesBlockLight) {
                    Lighting.setupFor3DItems()
                }

                if (hovered && showTooltip) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, item, mouseX, mouseY)
                }
            }
        },
        modifier = modifier.debug("Item: ${item.item}")
            .onPointerEvent(PointerEventType.ENTER) { _, _ -> hovered = true }
            .onPointerEvent(PointerEventType.EXIT) { _, _ -> hovered = false }
    )
}