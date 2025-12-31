//package com.digia.digiaui.framework.page
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import com.digia.digiaui.framework.LocalUIResources
//import com.digia.digiaui.framework.RenderPayload
//import com.digia.digiaui.framework.VirtualWidgetRegistry
//import com.digia.digiaui.framework.expr.DefaultScopeContext
//import com.digia.digiaui.framework.models.PageDefinition
//
///** DUIPage - renders a page from its definition Mirrors Flutter DUIPage */
//@Composable
//fun DUIPage(
//        pageId: String,
//        pageArgs: Map<String, Any?>?,
//        pageDef: PageDefinition,
//        registry: VirtualWidgetRegistry
//) {
//    val context = LocalContext.current
//    val resources = LocalUIResources.current
//
//    // Resolve page arguments
//    val resolvedPageArgs =
//            pageDef.pageArgDefs?.mapValues { (key, variable) ->
//                pageArgs?.get(key) ?: variable.defaultValue
//            }
//                    ?: emptyMap()
//
//    // Create initial state
//    val resolvedState =
//            pageDef.initStateDefs?.mapValues { (_, variable) -> variable.defaultValue }
//                    ?: emptyMap()
//
//    // Create scope context
//    val scopeContext = DefaultScopeContext(resolvedPageArgs + resolvedState)
//
//    // Get root widget
//    val rootNode = pageDef.layout?.root
//    if (rootNode == null) {
//        // Empty page
//        return
//    }
//
//    // Create virtual widget
//    val virtualWidget = registry.createWidget(rootNode)
//
//    // Create render payload
//    val payload =
//            RenderPayload(context = context, scopeContext = scopeContext, resources = resources)
//
//    // Render the widget
//    virtualWidget.toWidget(payload)
//}
