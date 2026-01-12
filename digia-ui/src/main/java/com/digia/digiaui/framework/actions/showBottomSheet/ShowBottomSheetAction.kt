package com.digia.digiaui.framework.actions.showBottomSheet

import android.content.Context
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike

/**
 * ShowBottomSheet Action
 * 
 * Displays a bottom sheet modal with specified content.
 * The content can be a widget tree or a page ID.
 * 
 * @param pageId The ID of the page to display in the bottom sheet
 * @param isDismissible Whether the bottom sheet can be dismissed by dragging (default: true)
 * @param enableDrag Whether drag gesture is enabled (default: true)
 * @param isScrollControlled Whether the bottom sheet takes full screen height (default: false)
 * @param backgroundColor Background color of the bottom sheet
 */
data class ShowBottomSheetAction(
    override var actionId: ActionId? = null,
    override var disableActionIf: ExprOr<Boolean>? = null,
    val pageId: ExprOr<String>?,
    val isDismissible: ExprOr<Boolean>? = null,
    val enableDrag: ExprOr<Boolean>? = null,
    val isScrollControlled: ExprOr<Boolean>? = null,
    val backgroundColor: ExprOr<String>? = null
) : Action {
    override val actionType = ActionType.SHOW_BOTTOM_SHEET

    override fun toJson(): JsonLike =
        mapOf(
            "type" to actionType.value,
            "pageId" to pageId?.toJson(),
            "isDismissible" to isDismissible?.toJson(),
            "enableDrag" to enableDrag?.toJson(),
            "isScrollControlled" to isScrollControlled?.toJson(),
            "backgroundColor" to backgroundColor?.toJson()
        )

    companion object {
        fun fromJson(json: JsonLike): ShowBottomSheetAction {
            return ShowBottomSheetAction(
                pageId = ExprOr.fromValue(json["pageId"]),
                isDismissible = ExprOr.fromValue(json["isDismissible"]),
                enableDrag = ExprOr.fromValue(json["enableDrag"]),
                isScrollControlled = ExprOr.fromValue(json["isScrollControlled"]),
                backgroundColor = ExprOr.fromValue(json["backgroundColor"])
            )
        }
    }
}

/** Processor for show bottom sheet action */
class ShowBottomSheetProcessor : ActionProcessor<ShowBottomSheetAction>() {
    override fun execute(
        context: Context,
        action: ShowBottomSheetAction,
        scopeContext: ScopeContext?,
        stateContext: com.digia.digiaui.framework.state.StateContext?,
        id: String
    ) {
        // Evaluate pageId
        val pageId = action.pageId?.evaluate(scopeContext) ?: ""
        if (pageId.isEmpty()) {
            println("ShowBottomSheetAction: pageId is empty")
            return
        }

        // Evaluate options
        val isDismissible = action.isDismissible?.evaluate(scopeContext) ?: true
        val enableDrag = action.enableDrag?.evaluate(scopeContext) ?: true
        val isScrollControlled = action.isScrollControlled?.evaluate(scopeContext) ?: false
        val backgroundColor = action.backgroundColor?.evaluate<String>(scopeContext)

        println("ShowBottomSheetAction: Showing bottom sheet with pageId: $pageId")
        println("  isDismissible: $isDismissible, enableDrag: $enableDrag")
        println("  isScrollControlled: $isScrollControlled, backgroundColor: $backgroundColor")

        // TODO: Implement bottom sheet display
        // This requires integration with Compose ModalBottomSheet
        // The implementation would need to:
        // 1. Get the BottomSheetState from CompositionLocal
        // 2. Load the page content using pageId
        // 3. Show the bottom sheet with the configured options
        // 4. Handle dismissal and callbacks
    }
}
