//package com.digia.digiaui.framework.actions.appstate
//
//import android.content.Context
//import com.digia.digiaui.framework.actions.base.Action
//import com.digia.digiaui.framework.actions.base.ActionId
//import com.digia.digiaui.framework.actions.base.ActionProcessor
//import com.digia.digiaui.framework.actions.base.ActionType
//import com.digia.digiaui.framework.appstate.AppStateManager
//import com.digia.digiaui.framework.expr.ScopeContext
//import com.digia.digiaui.framework.models.ExprOr
//import com.digia.digiaui.framework.utils.JsonLike
//
///**
// * SetAppState Action
// *
// * Updates global application state.
// * Unlike SetState which works on page-scoped state, this updates app-level state.
// *
// * @param updates Map of state variable names to their new values (can be expressions)
// */
//data class SetAppStateAction(
//    override var actionId: ActionId? = null,
//    override var disableActionIf: ExprOr<Boolean>? = null,
//    val updates: Map<String, ExprOr<Any>?>
//) : Action {
//    override val actionType = ActionType.SET_APP_STATE
//
//    override fun toJson(): JsonLike {
//        return mapOf(
//            "type" to actionType.value,
//            "updates" to updates.mapValues { it.value.toJson() }
//        )
//    }
//
//    companion object {
//        fun fromJson(json: JsonLike): SetAppStateAction? {
//            val updatesJson = json["updates"] as? Map<*, *> ?: return null
//
//            val updates = updatesJson.mapNotNull { (key, value) ->
//                val keyStr = key as? String ?: return@mapNotNull null
//                keyStr to ExprOr.fromValue<Any>(value)
//            }.toMap()
//
//            return SetAppStateAction(updates = updates)
//        }
//    }
//}
//
///**
// * SetAppState Action Processor
// */
//class SetAppStateProcessor : ActionProcessor<SetAppStateAction>() {
//    override fun execute(
//        context: Context,
//        action: SetAppStateAction,
//        scopeContext: ScopeContext?,
//        stateContext: com.digia.digiaui.framework.state.StateContext?,
//        id: String
//    ) {
//        // Evaluate all expressions
//        val evaluatedUpdates = action.updates.mapValues { (_, expr) ->
//            expr.evaluate(scopeContext)
//        }
//
//        // Update app state
//        AppStateManager.setAll(evaluatedUpdates)
//
//        println("SetAppStateAction: Updated ${evaluatedUpdates.size} app state variables")
//        evaluatedUpdates.forEach { (key, value) ->
//            println("  $key = $value")
//        }
//    }
//}
//
///**
// * GetAppState Action
// *
// * Retrieves a value from global app state and stores it in page state.
// * Useful for syncing app state to page state.
// *
// * @param appStateKey The key in app state to read from
// * @param targetStateKey The key in page state to write to
// */
//data class GetAppStateAction(
//    override var actionId: ActionId? = null,
//    override var disableActionIf: ExprOr<Boolean>? = null,
//    val appStateKey: ExprOr<String>?,
//    val targetStateKey: ExprOr<String>?
//) : Action {
//    override val actionType = ActionType.GET_APP_STATE
//
//    override fun toJson(): JsonLike {
//        return mapOf(
//            "type" to actionType.value,
//            "appStateKey" to appStateKey?.toJson(),
//            "targetStateKey" to targetStateKey?.toJson()
//        )
//    }
//
//    companion object {
//        fun fromJson(json: JsonLike): GetAppStateAction {
//            return GetAppStateAction(
//                appStateKey = ExprOr.fromValue(json["appStateKey"]),
//                targetStateKey = ExprOr.fromValue(json["targetStateKey"])
//            )
//        }
//    }
//}
//
///**
// * GetAppState Action Processor
// */
//class GetAppStateProcessor : ActionProcessor<GetAppStateAction>() {
//    override fun execute(
//        context: Context,
//        action: GetAppStateAction,
//        scopeContext: ScopeContext?,
//        stateContext: com.digia.digiaui.framework.state.StateContext?,
//        id: String
//    ) {
//        val appStateKey = action.appStateKey?.evaluate(scopeContext) ?: return
//        val targetStateKey = action.targetStateKey?.evaluate(scopeContext) ?: appStateKey
//
//        // Get value from app state
//        val value = AppStateManager.get(appStateKey)
//
//        // Set in page state
//        stateContext?.set(targetStateKey, value)
//
//        println("GetAppStateAction: Copied $appStateKey to $targetStateKey = $value")
//    }
//}
//
///**
// * ResetAppState Action
// *
// * Resets all app state to initial values from configuration.
// * Useful for logout or app reset scenarios.
// */
//data class ResetAppStateAction(
//    override var actionId: ActionId? = null,
//    override var disableActionIf: ExprOr<Boolean>? = null
//) : Action {
//    override val actionType = ActionType.RESET_APP_STATE
//
//    override fun toJson(): JsonLike {
//        return mapOf("type" to actionType.value)
//    }
//
//    companion object {
//        fun fromJson(json: JsonLike): ResetAppStateAction {
//            return ResetAppStateAction()
//        }
//    }
//}
//
///**
// * ResetAppState Action Processor
// */
//class ResetAppStateProcessor : ActionProcessor<ResetAppStateAction>() {
//    override fun execute(
//        context: Context,
//        action: ResetAppStateAction,
//        scopeContext: ScopeContext?,
//        stateContext: com.digia.digiaui.framework.state.StateContext?,
//        id: String
//    ) {
//        AppStateManager.reset()
//        println("ResetAppStateAction: App state reset to initial values")
//    }
//}
