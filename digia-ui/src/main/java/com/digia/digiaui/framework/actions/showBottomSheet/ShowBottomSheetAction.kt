package com.digia.digiaui.framework.actions.showBottomSheet

import android.content.Context
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionFlow
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.state.StateContext
import com.digia.digiaui.framework.utils.JsonLike
import kotlinx.coroutines.launch


/**
 * ShowBottomSheet Action
 *
 * Displays a bottom sheet modal with specified content.
 */
data class ShowBottomSheetAction(
    override var actionId: ActionId? = null,
    override var disableActionIf: ExprOr<Boolean>? = null,
    val viewData: ExprOr<JsonLike>?,
    val waitForResult: Boolean = false,
    val style: JsonLike?,
    val onResult: ActionFlow?
) : Action {
    override val actionType = ActionType.SHOW_BOTTOM_SHEET

    override fun toJson(): JsonLike =
        mapOf(
            "type" to actionType.value,
            "viewData" to viewData?.toJson(),
            "waitForResult" to waitForResult,
            "style" to style,
            "onResult" to onResult?.toJson()
        )

    companion object {
        fun fromJson(json: JsonLike): ShowBottomSheetAction {
            return ShowBottomSheetAction(
                viewData = ExprOr.fromJson<JsonLike>(json["viewData"]),
                waitForResult = json["waitForResult"] as? Boolean ?: false,
                style = json["style"] as? JsonLike,
                onResult = (json["onResult"] as? JsonLike)?.let { ActionFlow.fromJson(it) }
            )
        }
    }
}


/** Processor for show bottom sheet action */
class ShowBottomSheetProcessor : ActionProcessor<ShowBottomSheetAction>() {
    override suspend fun execute(
        context: Context,
        action: ShowBottomSheetAction,
        scopeContext: ScopeContext?,
        stateContext: StateContext?,
        resourceProvider: com.digia.digiaui.framework.UIResources?,
        id: String
    ): Any? {
        // Evaluate viewData to get component/view ID and arguments
        val viewData = action.viewData?.evaluate<JsonLike>(scopeContext)
        if (viewData == null) {
            android.util.Log.e("ShowBottomSheet", "viewData is null")
            return null
        }

        val componentId = viewData["id"] as? String
        if (componentId.isNullOrEmpty()) {
            android.util.Log.e("ShowBottomSheet", "componentId is empty")
            return null
        }

        val args = viewData["args"] as? JsonLike
        val style = action.style ?: emptyMap()

        // Extract style properties
        val bgColorStr = (style["bgColor"] as? String)?.let {
            ExprOr.fromJson<String>(it)?.evaluate(scopeContext)
        } ?: style["bgColor"] as? String

        val barrierColorStr = (style["barrierColor"] as? String)?.let {
            ExprOr.fromJson<String>(it)?.evaluate(scopeContext)
        } ?: style["barrierColor"] as? String

        val maxHeightRatio = ((style["maxHeight"] as? Number)?.toDouble()
            ?: ExprOr.fromJson<Number>(style["maxHeight"])?.evaluate<Number>(scopeContext)?.toDouble())
            ?: 1.0

        val useSafeArea = (style["useSafeArea"] as? Boolean) ?: true

        // Get the bottom sheet manager from DigiaUIManager
        val bottomSheetManager = com.digia.digiaui.init.DigiaUIManager.getInstance().bottomSheetManager

        // Show the bottom sheet
        bottomSheetManager?.show(
            componentId = componentId,
            args = args,
            backgroundColor = bgColorStr,
            barrierColor = barrierColorStr,
            maxHeightRatio = maxHeightRatio.toFloat(),
            useSafeArea = useSafeArea,
            onDismiss = { result ->
                // Handle result if waitForResult is true
                if (action.waitForResult && action.onResult != null) {
                    // Execute onResult callback with the result
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        val resultContext = com.digia.digiaui.framework.expr.DefaultScopeContext(
                            variables = mapOf("result" to result),
                            enclosing = scopeContext
                        )
                        com.digia.digiaui.framework.actions.ActionExecutor().execute(
                            context = context,
                            actionFlow = action.onResult,
                            scopeContext = resultContext,
                            stateContext = stateContext,
                            resourcesProvider = resourceProvider
                        )
                    }
                }
            }
        )

        return null
    }
}
