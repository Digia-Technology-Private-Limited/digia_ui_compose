package com.digia.digiaui.framework

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RocketLaunch
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.expression.evaluate
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.network.APIModel
import defaultTextStyle
import makeTextStyle
import resourceApiModel
import resourceColor
import resourceFontFactory

data class RenderPayload(
        val scopeContext: ScopeContext?,
        val widgetHierarchy: List<String> = emptyList(),
        val currentEntityId: String? = null
) {

    /* ---------------- Expression evaluation ---------------- */

    inline fun <reified T : Any> evalExpr(
            expr: ExprOr<T>?,
            noinline decoder: ((Any) -> T?)? = null
    ): T? = expr?.evaluate(scopeContext, decoder)

    inline fun <reified T : Any> eval(
            expression: Any?,
            scopeContext: ScopeContext? = null,
            noinline decoder: ((Any?) -> T?)? = null
    ): T? = evaluate(expression, chainExprContext(scopeContext), decoder)

    /* ---------------- Hierarchy helpers ---------------- */

    fun withExtendedHierarchy(widgetName: String): RenderPayload =
            copy(widgetHierarchy = widgetHierarchy + widgetName)

    fun forComponent(componentId: String): RenderPayload =
            copy(widgetHierarchy = widgetHierarchy + componentId, currentEntityId = componentId)

    /* ---------------- Context chaining ---------------- */

    fun chainExprContext(incoming: ScopeContext?): ScopeContext? {
        if (incoming == null) return scopeContext
        if (scopeContext == null) return incoming

        incoming.enclosing = scopeContext
        return incoming
    }

    fun copyWithChainedContext(
            scopeContext: ScopeContext,
    ): RenderPayload = copy(scopeContext = chainExprContext(scopeContext))

    /* ---------------- Action execution ---------------- */

    /**
     * Executes an action flow with optional trigger context Currently a stub - will be implemented
     * when action executor is available
     */
    fun executeAction(actionJson: JsonLike?, triggerType: String? = null) {
        if (actionJson == null) return

        // TODO: Implement action execution when ActionExecutor is ready
        // For now, this is a no-op to prevent compilation errors
        // val actionFlow = ActionFlow.fromJson(actionJson)
        // actionExecutor.execute(actionFlow, scopeContext, triggerType)
    }
}

@Composable fun RenderPayload.color(key: String): Color? = resourceColor(key)

@Composable fun RenderPayload.apiModel(id: String): APIModel? = resourceApiModel(id)

@Composable fun RenderPayload.fontFactory(): DUIFontFactory? = resourceFontFactory()

@Composable
fun RenderPayload.textStyle(
        token: Map<String, Any?>?,
        fallback: TextStyle? = defaultTextStyle
): TextStyle? = makeTextStyle(token, { expr -> eval<String>(expr) }, fallback)

@Composable
fun RenderPayload.evalColor(expression: Any?, scopeContext: ScopeContext? = null): Color? =
        eval<String>(expression, scopeContext)?.let { resourceColor(it) }


        /* ---------- Helpers: icon resolution ---------- */

/**
 * Resolve iconData JSON ({ "pack": "material", "key": "rocket", ... })
 * into an ImageVector. Extend this as you add more packs/keys.
 */
@Composable
fun RenderPayload.getIcon(iconData: JsonLike?): ImageVector? {
    iconData ?: return null

    val pack = iconData["pack"] as? String ?: return null
    val key = iconData["key"] as? String ?: return null

    return when (pack) {
        "material" -> materialIconFromKey(key)
        else -> null
    }
}

/**
 * Basic mapping for Material icon keys.
 * Add more cases here as your schema uses more icons.
 */
private fun materialIconFromKey(key: String): ImageVector? =
    when (key) {
        "rocket" -> Icons.Outlined.RocketLaunch
        else -> null
    }