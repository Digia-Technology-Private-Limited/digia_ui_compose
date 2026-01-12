package com.digia.digiaui.framework.actions.navigation

import android.content.Context
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.navigation.NavigationManager
import com.digia.digiaui.framework.utils.JsonLike

/**
 * PopPage Action
 *
 * Pops the current page from the navigation stack.
 * Returns to the previous page.
 */
data class PopPageAction(
    override var actionId: ActionId? = null,
    override var disableActionIf: ExprOr<Boolean>? = null,
    val result: ExprOr<Any>? = null
) : Action {
    override val actionType = ActionType.NAVIGATE_BACK

    override fun toJson(): JsonLike {
        return mapOf(
            "type" to actionType.value,
            "result" to result?.toJson()
        )
    }

    companion object {
        fun fromJson(json: JsonLike): PopPageAction {
            return PopPageAction(
                result = json["result"]?.let { ExprOr.fromValue<Any>(it) }
            )
        }
    }
}

/** PopPage Action Processor */
class PopPageProcessor : ActionProcessor<PopPageAction>() {
    override fun execute(
        context: Context,
        action: PopPageAction,
        scopeContext: ScopeContext?,
        stateContext: com.digia.digiaui.framework.state.StateContext?,
        id: String
    ): Any? {
        // Evaluate result if provided
        val result = action.result?.evaluate<Any>(scopeContext)

        println("PopPageAction: Pop with result: $result")
        
        // Store pop request in navigation manager
        NavigationManager.pop(result)

        return null
    }
}
