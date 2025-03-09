package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import net.minecraft.resources.ResourceLocation
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.util.ComposableTheme
import xyz.milosworks.klib.ui.util.NPTResourceLoader

object ThemeTypes {
    const val JAVA = "java"
    const val BEDROCK = "bedrock"
}

object ThemeVariants {
    const val DEFAULT = ""
    const val DARK = "dark"
}

@Immutable
data class ThemeData(
    val mode: String,
    val type: String,
    val namespace: String = KLib.ID,
) {
    @Suppress("NOTHING_TO_INLINE")
    inline fun getComposableTheme(composable: String): ComposableTheme =
        ComposableTheme[composableThemeLocation(namespace, type, composable)]
}

val LocalTheme = compositionLocalOf { ThemeData(ThemeVariants.DEFAULT, ThemeTypes.JAVA, KLib.ID) }

@Suppress("NOTHING_TO_INLINE")
inline fun composableThemeLocation(
    namespace: String,
    type: String,
    composable: String,
): ResourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, composable)
    .run { if (type.isNotEmpty()) withPrefix("$type/") else this }

@Deprecated("Use composableThemeLocation instead")
@Suppress("NOTHING_TO_INLINE")
inline fun buildThemeTextureLocation(
    namespace: String,
    type: String,
    composable: String,
    state: String,
    mode: String,
): ResourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, composable)
    .run { if (type.isNotEmpty()) withPrefix("$type/") else this }
    .withSuffix("/$state")
    .run { if (mode.isNotEmpty()) withSuffix("_$mode") else this }

@Deprecated("Use composableThemeLocation instead")
@Suppress("NOTHING_TO_INLINE")
inline fun themeTextureLocation(
    namespace: String,
    type: String,
    composable: String,
    state: String,
    mode: String,
): ResourceLocation = buildThemeTextureLocation(namespace, type, composable, state, mode)
    .takeIf { NPTResourceLoader.TEXTURES[it] != null }
    ?: buildThemeTextureLocation(namespace, type, composable, TextureStates.DEFAULT, mode)
        .takeIf { NPTResourceLoader.TEXTURES[it] != null }
    ?: buildThemeTextureLocation(KLib.ID, ThemeTypes.JAVA, composable, TextureStates.DEFAULT, ThemeVariants.DEFAULT)

@Deprecated("Use composableThemeLocation instead")
@Suppress("NOTHING_TO_INLINE")
inline fun hasThemeTexture(
    namespace: String, type: String, composable: String, state: String, mode: String
): Boolean =
    buildThemeTextureLocation(
        namespace,
        type,
        composable,
        state,
        mode
    ).run { NPTResourceLoader.TEXTURES[this] != null }

@Composable
fun Theme(
    mode: String = ThemeVariants.DEFAULT,
    type: String = ThemeTypes.JAVA,
    namespace: String = KLib.ID,
    content: @Composable () -> Unit
) =
    CompositionLocalProvider(LocalTheme provides ThemeData(mode, type, namespace)) { content() }

@Composable
fun Theme(data: ThemeData, content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalTheme provides data) { content() }