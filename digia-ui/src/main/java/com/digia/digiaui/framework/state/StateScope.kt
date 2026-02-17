package com.digia.digiaui.framework.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
val LocalStateTree = compositionLocalOf { StateTree() }

@Composable
fun StateScope(
    namespace: String?,
    initialState: Map<String, Any?> = emptyMap(),
    content: @Composable (stateContext: StateContext) -> Unit
) {
    // Use existing tree or create a new one
    val tree = LocalStateTree.current
    val parentContext = LocalStateContextProvider.current

    // Check if StateContext already exists for this namespace
    val stateContext = remember(namespace) {
        val existing = namespace?.let { tree.getByNamespace(it) }
        if (existing != null) {
            existing
        } else {
            val newContext = StateContext(
                namespace = namespace,
                tree = tree,
                initialState = initialState
            )
            // Attach to tree with parent
            tree.attach(parentContext, newContext)
            newContext
        }
    }

    // Don't dispose on navigation - let the StateTree persist for back navigation
    // StateContext will be cleaned up when the StateTree itself is removed

    CompositionLocalProvider(
        LocalStateContextProvider provides stateContext,
    ) {
        // Read version to trigger recomposition, but don't use key() to avoid full rebuild
        stateContext.Version()
        content(stateContext)
    }
}
