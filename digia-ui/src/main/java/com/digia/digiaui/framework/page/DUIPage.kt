package com.digia.digiaui.framework.page

import LocalUIResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.digia.digiaexpr.std.StdLibFunctions
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.appstate.AppStateScopeContext
import com.digia.digiaui.framework.appstate.DUIAppState
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.PageDefinition
import com.digia.digiaui.framework.state.LocalStateTree
import com.digia.digiaui.framework.state.StateContext
import com.digia.digiaui.framework.state.StateScope
import com.digia.digiaui.framework.state.StateScopeContext
import com.digia.digiaui.framework.state.StateTree
import com.digia.digiaui.init.DigiaUIManager

/** DUIPage - renders a page from its definition Mirrors Flutter DUIPage */
@Composable
fun DUIPage(
        pageId: String,
        pageArgs: Map<String, Any?>?,
        pageDef: PageDefinition,
        registry: VirtualWidgetRegistry
) {
    // Resolve page arguments
    val resolvedPageArgs =
            pageDef.pageArgDefs?.mapValues { (key, variable) ->
                pageArgs?.get(key) ?: variable.defaultValue
            }
                    ?: emptyMap()

    // Create initial state
    val resolvedState =
            pageDef.initStateDefs?.mapValues { (_, variable) -> variable.defaultValue }
                    ?: emptyMap()

    // Create scope context

    // Get root widget
    val rootNode = pageDef.layout?.root
    if (rootNode == null) {
        // Empty page
        return
    }

    // Create virtual widget
    val virtualWidget = registry.createWidget(rootNode,null)

    val context = AppStateScopeContext(
        values = DUIAppState.instance.all(),
        variables = mutableMapOf<String, Any?>().apply {
            putAll(StdLibFunctions.functions)
            putAll(DigiaUIManager.getInstance().jsVars)
        }
    )


    // Render the widget
    RootStateTreeProvider {
        StateScope(namespace = pageId, initialState = resolvedState) { stateContext ->
            virtualWidget.ToWidget(  RenderPayload( scopeContext = _createExprContext(params = resolvedPageArgs,
                stateContext = stateContext,
                scopeContext = context
                )
            ))
        }
    }
}


@Composable
fun RootStateTreeProvider(content: @Composable () -> Unit) {
    val tree = remember { StateTree() } // single tree for entire app/session

    CompositionLocalProvider(
        LocalStateTree provides tree
    ) {
        content()
    }
}


internal fun _createExprContext(params:Map<String, Any?>,stateContext: StateContext?
                               ,scopeContext: ScopeContext
): ScopeContext {

    val pageVariables = mapOf(
        // Backward compatibility key
        "pageParams" to params,
        // New convention: spread the params map into the new map
        *params.toList().toTypedArray()
    )

    if (stateContext == null) {
        return DefaultScopeContext(
            name = "",
           variables = pageVariables,
          enclosing =scopeContext
        );
    }

    return StateScopeContext(
        state= stateContext,
        variables = pageVariables,
        enclosing= scopeContext,
    );

}