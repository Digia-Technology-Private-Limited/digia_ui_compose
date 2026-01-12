package com.digia.digiaui.framework.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.Variable


/**
 * Virtual State Container Widget
 *
 * Provides scoped state management for child widgets. Similar to Flutter's StatefulScopeWidget.
 * Creates a StateContext with initial state and provides it to child widgets through
 * the render payload's scope context.
 */
class VWStateContainer(
    refName: String?,
    parent: VirtualNode?,
    parentProps: Props?,
    private val initStateDefs: Map<String, Variable>,
    private val childGroups: Map<String, List<VirtualNode>>?
) : VirtualNode(refName, parent, parentProps) {

    // Get the first child from childGroups (similar to Flutter implementation)
    private val child: VirtualNode? = childGroups?.entries?.firstOrNull()?.value?.firstOrNull()

    @Composable
    override fun Render(payload: RenderPayload) {
        if (child == null) {
            Empty()
            return
        }

        // Resolve initial state values by evaluating expressions/defaults
        val resolvedState = initStateDefs.mapValues { (_, variable) ->
            // Try to evaluate default value as expression, fallback to literal value
            variable.defaultValue
        }

        // Create stateful scope widget that provides state context
        StateScope(
            namespace = refName,
            initialState = resolvedState,
        ) { stateContext ->
    child.ToWidget(payload.copyWithChainedContext(_createExprContext(stateContext = stateContext)))
        }

    }

    fun _createExprContext(
                           stateContext: StateContext) : StateScopeContext {
        return StateScopeContext(
            state = stateContext,
        )
    }


    @Composable
    override fun Modifier.buildModifier(payload: RenderPayload): Modifier {
        return this // State containers don't modify the layout
    }
}

