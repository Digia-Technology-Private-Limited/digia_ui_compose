package com.digia.digiaui.framework.actions

//import com.digia.digiaui.framework.actions.appstate.GetAppStateProcessor
//import com.digia.digiaui.framework.actions.appstate.ResetAppStateProcessor
//import com.digia.digiaui.framework.actions.appstate.SetAppStateProcessor
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
//import com.digia.digiaui.framework.actions.callRestApi.CallRestApiProcessor
import com.digia.digiaui.framework.actions.navigation.GotoPageProcessor
import com.digia.digiaui.framework.actions.navigation.PopPageProcessor
import com.digia.digiaui.framework.actions.openUrl.OpenUrlProcessor
import com.digia.digiaui.framework.actions.rebuildState.RebuildStateProcessor
import com.digia.digiaui.framework.actions.setState.SetStateProcessor
import com.digia.digiaui.framework.actions.showBottomSheet.ShowBottomSheetProcessor
import com.digia.digiaui.framework.actions.showToast.ShowToastProcessor

/** Action processor factory - routes actions to their processors */
class ActionProcessorFactory {
    fun getProcessor(action: Action): ActionProcessor<*> {
        return when (action.actionType) {
            ActionType.SHOW_TOAST -> ShowToastProcessor()
            ActionType.SET_STATE -> SetStateProcessor()
            ActionType.REBUILD_STATE -> RebuildStateProcessor()
            ActionType.NAVIGATE_TO_PAGE -> GotoPageProcessor()
            ActionType.NAVIGATE_BACK -> PopPageProcessor()
            ActionType.OPEN_URL -> OpenUrlProcessor()
//            ActionType.CALL_REST_API -> CallRestApiProcessor()
            ActionType.SHOW_BOTTOM_SHEET -> ShowBottomSheetProcessor()
//            ActionType.SET_APP_STATE -> SetAppStateProcessor()
//            ActionType.GET_APP_STATE -> GetAppStateProcessor()
//            ActionType.RESET_APP_STATE -> ResetAppStateProcessor()
            // Other action types will be added here
            else -> throw IllegalArgumentException("Unsupported action type: ${action.actionType}")
        }
    }
}