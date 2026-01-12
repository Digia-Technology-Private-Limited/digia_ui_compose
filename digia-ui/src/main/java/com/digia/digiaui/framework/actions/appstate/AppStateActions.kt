package com.digia.digiaui.framework.actions.appstate

import android.content.Context
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.UIResources
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.appstate.DUIAppState
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.state.StateContext
import com.digia.digiaui.framework.utils.JsonLike

/**
 * SetAppState Action
 *
 * Updates global application state.
 * Unlike SetState which works on page-scoped state, this updates app-level state.
 *
 * @param updates Map of state variable names to their new values (can be expressions)
 */
data class SetAppStateAction(
    override var actionId: ActionId? = null,
    override var disableActionIf: ExprOr<Boolean>? = null,
    val updates: Map<String, ExprOr<Any>?>
) : Action {
    override val actionType = ActionType.SET_APP_STATE

    override fun toJson(): JsonLike {
        return mapOf(
            "type" to actionType.value,
            "updates" to updates.mapValues { it.value?.toJson() }
        )
    }

    companion object {
        fun fromJson(json: JsonLike): SetAppStateAction? {
            val updatesJson = json["updates"] as? Map<*, *> ?: return null

            val updates = updatesJson.mapNotNull { (key, value) ->
                val keyStr = key as? String ?: return@mapNotNull null
                keyStr to ExprOr.fromValue<Any>(value)
            }.toMap()

            return SetAppStateAction(updates = updates)
        }
    }
}

/**
 * SetAppState Action Processor
 */
class SetAppStateProcessor : ActionProcessor<SetAppStateAction>() {
    override fun execute(
        context: Context,
        action: SetAppStateAction,
        scopeContext: ScopeContext?,
        stateContext: StateContext?,
        resourceProvider: UIResources?,
        id: String
    ): Any? {
         try {
             for (update in action.updates) {
                 val key = update.key
                 val value = update.value?.evaluate<Any>(scopeContext)
                 DUIAppState.instance.update(key, value)
                 println("SetAppStateAction: Set $key = $value")
             }
                return true
         } catch(e: Exception) {
             return false
         }
    }
}

