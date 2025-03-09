package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Size(
	val width: Int = 0,
	val height: Int = 0
)