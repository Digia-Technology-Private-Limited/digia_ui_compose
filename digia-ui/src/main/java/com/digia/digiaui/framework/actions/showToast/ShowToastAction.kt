package com.digia.digiaui.framework.actions.showToast

import android.content.Context
import android.widget.Toast
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike

/** Show toast action */
data class ShowToastAction(
        override var actionId: ActionId? = null,
        override var disableActionIf: ExprOr<Boolean>? = null,
        val message: ExprOr<String>?,
        val duration: ExprOr<String>? = null
) : Action {
    override val actionType = ActionType.SHOW_TOAST

    override fun toJson(): JsonLike =
            mapOf(
                    "type" to actionType.value,
                    "message" to
                            message?.toJson(),
                    "duration" to
                           duration?.toJson(),
            )

    companion object {
        fun fromJson(json: JsonLike): ShowToastAction {
            return ShowToastAction(
                    message = ExprOr.fromValue(json["message"]),
                    duration = ExprOr.fromValue(json["duration"])
            )
        }
    }
}

/** Processor for show toast action */
class ShowToastProcessor : ActionProcessor<ShowToastAction>() {
    override fun execute(
            context: Context,
            action: ShowToastAction,
            scopeContext: ScopeContext?,
            stateContext: com.digia.digiaui.framework.state.StateContext?,
            id: String
    ) {
        // Evaluate message
        val message = action.message?.evaluate(scopeContext) ?: ""

        // Evaluate duration
        val durationStr = action.duration?.evaluate(scopeContext) ?: "short"
        val toastDuration =
            when (durationStr.lowercase()) {
                "long" -> Toast.LENGTH_LONG
                else -> Toast.LENGTH_SHORT
            }

        // Show toast
        Toast.makeText(context, message.toString(), toastDuration).show()


    }
}
