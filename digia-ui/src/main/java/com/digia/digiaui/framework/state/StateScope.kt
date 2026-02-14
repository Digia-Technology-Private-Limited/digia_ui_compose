package com.digia.digiaui.framework.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

val LocalStateTree = compositionLocalOf { StateTree() }

/**
 * Saver for StateContext that preserves state values across navigation Saves all values - they
 * persist during navigation (in-memory) Note: Only primitive types will survive process death
 */
fun stateContextSaver(tree: StateTree, namespace: String?): Saver<StateContext, Map<String, Any?>> =
        Saver(
                save = { context ->
                    // Save all state values
                    context.snapshot()
                },
                restore = { savedValues ->
                    // Restore StateContext with saved values
                    StateContext(namespace = namespace, tree = tree, initialState = savedValues)
                }
        )

@Composable
fun StateScope(
        namespace: String?,
        initialState: Map<String, Any?> = emptyMap(),
        content: @Composable (stateContext: StateContext) -> Unit
) {
    // Use existing tree or create a new one
    val tree = LocalStateTree.current

        // Parent is the nearest enclosing StateScope (if any)
        val parentStateContext = LocalStateContextProvider.current

    // Use rememberSaveable with custom Saver to persist StateContext across navigation
    val stateContext =
            rememberSaveable(
                    inputs = arrayOf(namespace),
                    saver = stateContextSaver(tree, namespace)
            ) { StateContext(namespace = namespace, tree = tree, initialState = initialState) }

    // Maintain the parent-child relationship in the shared StateTree.
    // Required for resolving owners across scopes and for observe()/flush propagation.
    DisposableEffect(tree, parentStateContext, stateContext) {
        tree.attach(parentStateContext, stateContext)
        onDispose { tree.detach(stateContext) }
    }

    DisposableEffect(stateContext) { onDispose { stateContext.dispose() } }

    CompositionLocalProvider(
            LocalStateContextProvider provides stateContext,
    ) {
        // Read version to trigger recomposition
        stateContext.Version()
        content(stateContext)
    }
}
