package xyz.milosworks.klib.ui.components.basic

import androidx.compose.runtime.Composable
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3f
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.measure.MeasureResult
import xyz.milosworks.klib.ui.layout.measure.Renderer
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.debug

@Composable
fun Block(
    state: BlockState,
    entity: BlockEntity? = null,
    modifier: Modifier = Modifier
) {
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
                guiGraphics.pose().pushPose()

                guiGraphics.pose().translate(x + node.width / 2f, y + node.height / 2f, 100f)
                guiGraphics.pose().scale(40 * node.width / 64f, -40 * node.height / 64f, 40f)

                guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(30f))
                guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(45 + 180f))

                guiGraphics.pose().translate(-.5, -.5, -.5)

                @Suppress("DEPRECATION")
                RenderSystem.runAsFancy {
                    val consumers = Minecraft.getInstance().renderBuffers().bufferSource()
                    if (state.renderShape != RenderShape.ENTITYBLOCK_ANIMATED) {
                        Minecraft.getInstance().blockRenderer.renderSingleBlock(
                            state, guiGraphics.pose(), consumers,
                            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
                        )
                    }

                    if (entity != null) {
                        Minecraft.getInstance().blockEntityRenderDispatcher.getRenderer(entity)?.render(
                            entity,
                            partialTick,
                            guiGraphics.pose(),
                            consumers,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY
                        )
                    }

                    RenderSystem.setShaderLights(Vector3f(-1.5f, -.5f, 0f), Vector3f(0f, -1f, 0f))
                    consumers.endBatch()
                    Lighting.setupFor3DItems()
                }

                guiGraphics.pose().popPose()
            }
        },
        modifier = modifier.debug(*buildList {
            add("State: ${BuiltInRegistries.BLOCK.wrapAsHolder(state.block).registeredName}")
            if (entity != null)
                add("Entity: ${BuiltInRegistries.BLOCK.wrapAsHolder(entity.blockState.block).registeredName} ${entity.blockPos.x},${entity.blockPos.y},${entity.blockPos.z}")
        }.toTypedArray()),
    )
}