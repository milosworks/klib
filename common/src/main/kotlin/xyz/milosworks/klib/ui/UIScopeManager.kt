package xyz.milosworks.klib.ui

import kotlinx.coroutines.CoroutineScope

object UIScopeManager {
	val scopes = mutableSetOf<CoroutineScope>()
}