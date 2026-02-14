package com.digia.digiaui.framework.state

import LocalUIResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.actions.LocalActionExecutor
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.datatype.DataTypeCreator
import com.digia.digiaui.framework.datatype.Variable
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.navigation.NavigationManager

/**
 * Virtual State Container Widget
 *
 * Provides scoped state management for child widgets. Similar to Flutter's StatefulScopeWidget.
 * Creates a StateContext with initial state and provides it to child widgets through the render
 * payload's scope context.
 */
class VWStateContainer(
        refName: String?,
        parent: VirtualNode?,
        parentProps: Props?,
        private val initStateDefs: Map<String, Variable>,
        private val childGroups: Map<String, List<VirtualNode>>?
) : VirtualNode(refName, parent, parentProps) {

    private val child: VirtualNode? = childGroups?.entries?.firstOrNull()?.value?.firstOrNull()

    @Composable
    override fun Render(payload: RenderPayload) {
        val child =
                child
                        ?: run {
                            Empty()
                            return
                        }

        // ✅ Evaluate initial state ONCE
        val resolvedState =
                initStateDefs.mapValues { DataTypeCreator.create(it.value, payload.scopeContext) }

        StateScope(namespace = refName, initialState = resolvedState) { stateContext ->
            val context = LocalContext.current
            val actionExecutor = LocalActionExecutor.current
            val resources = LocalUIResources.current

            val scopeContext = _createExprContext(stateContext)

            val containerPayload = payload.copyWithChainedContext(scopeContext)

            // Execute pending navigation result callback inside THIS StateScope (refName).
            // This fixes cases where GotoPageAction is triggered inside a nested StateScope
            // and the callback must update that same scope's live StateContext.
            LaunchedEffect(refName) {
                val executingNamespace = refName ?: return@LaunchedEffect
                val pending =
                        NavigationManager.consumePendingCallback(executingNamespace)
                                ?: return@LaunchedEffect
                val (lookupPageId, result) = pending

                val callback =
                        NavigationManager.getResultCallback(lookupPageId) ?: return@LaunchedEffect
                NavigationManager.removeResultCallback(lookupPageId)

                val enclosing = containerPayload.scopeContext
                val resultScopeContext =
                        if (enclosing != null) {
                            DefaultScopeContext(
                                    variables = mapOf("result" to result),
                                    enclosing = enclosing
                            )
                        } else {
                            DefaultScopeContext(variables = mapOf("result" to result))
                        }

                actionExecutor.execute(
                        context = context,
                        actionFlow = callback.onResult,
                        scopeContext = resultScopeContext,
                        stateContext = stateContext,
                        resourcesProvider = resources,
                        scope = this
                )
            }

            stateContext.Version()

            child.ToWidget(payload = containerPayload)
        }
    }

    fun _createExprContext(stateContext: StateContext): StateScopeContext {
        return StateScopeContext(
                state = stateContext,
        )
    }

    @Composable
    override fun Modifier.buildModifier(payload: RenderPayload): Modifier {
        return this // State containers don't modify the layout
    }
}
