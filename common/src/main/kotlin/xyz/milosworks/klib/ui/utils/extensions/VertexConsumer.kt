package xyz.milosworks.klib.ui.utils.extensions

import com.mojang.blaze3d.vertex.VertexConsumer
import org.joml.Matrix4f

fun VertexConsumer.addVertex(matrix: Matrix4f, x: Int, y: Int, z: Int): VertexConsumer =
    addVertex(matrix, x.toFloat(), y.toFloat(), z.toFloat())