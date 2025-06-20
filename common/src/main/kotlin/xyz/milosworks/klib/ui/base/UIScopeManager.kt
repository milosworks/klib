package xyz.milosworks.klib.ui.base

import kotlinx.coroutines.CoroutineScope

object UIScopeManager {
    val scopes = mutableSetOf<CoroutineScope>()
}