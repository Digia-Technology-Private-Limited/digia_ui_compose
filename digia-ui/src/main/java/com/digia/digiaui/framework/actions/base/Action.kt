package com.digia.digiaui.framework.actions.base

import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.JsonLike

/** Action types enum - defines all available actions */
enum class ActionType(val value: String) {
    SHOW_TOAST("Action.showToast"),
    SET_STATE("Action.setState"),
    NAVIGATE_TO_PAGE("Action.navigateToPage"),
    NAVIGATE_BACK("Action.pop"),
    SHOW_DIALOG("Action.openDialog"),
    SHOW_BOTTOM_SHEET("Action.showBottomSheet"),
    CALL_REST_API("Action.callRestApi");

    companion object {
        fun fromString(value: String): ActionType {
            return values().firstOrNull { it.value == value }
                    ?: throw IllegalArgumentException("Unknown action type: $value")
        }
    }
}

/** Action identifier */
data class ActionId(val id: String)

/** Base interface for all actions */
interface Action {
    val actionType: ActionType
    var actionId: ActionId?
    val disableActionIf: ExprOr<Boolean>?

    fun toJson(): JsonLike
}

/** Action flow - sequence of actions to execute */
data class ActionFlow(val actions: List<Action>) {
    companion object {
        fun fromJson(json: JsonLike): ActionFlow {
            val actionsList = json["actions"] as? List<*> ?: emptyList<Any>()
            val actions =
                    actionsList.mapNotNull { actionJson ->
                        parseAction(actionJson as? JsonLike ?: return@mapNotNull null)
                    }
            return ActionFlow(actions)
        }

        private fun parseAction(json: JsonLike): Action? {
            val typeStr = json["type"] as? String ?: return null
            val actionType =
                    try {
                        ActionType.fromString(typeStr)
                    } catch (e: IllegalArgumentException) {
                        return null // Skip unknown action types
                    }

            return when (actionType) {
                ActionType.SHOW_TOAST ->
                        com.digia.digiaui.framework.actions.showToast.ShowToastAction.fromJson(json)
                // Other action types will be implemented later
                else -> null
            }
        }
    }
}
