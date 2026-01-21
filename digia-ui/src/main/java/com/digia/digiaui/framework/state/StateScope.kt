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
    val state=LocalStateContextProvider.current?.Version()


    val stateContext = remember {
        StateContext(
            namespace = namespace,
            tree = tree,
            initialState = initialState
        )
    }

    DisposableEffect(stateContext) {
        onDispose { stateContext.dispose() }
    }

    CompositionLocalProvider(
        LocalStateContextProvider provides stateContext,
    ) {
        // Read version to trigger recomposition, but don't use key() to avoid full rebuild
        stateContext.Version()
        content(stateContext)
    }
}
