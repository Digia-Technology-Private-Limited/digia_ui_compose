package com.digia.digiaui.framework.actions

import android.content.Context
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionFlow
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.actions.showToast.ShowToastProcessor
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.expr.evaluate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Action processor factory - routes actions to their processors */
class ActionProcessorFactory {
    fun getProcessor(action: Action): ActionProcessor<*> {
        return when (action.actionType) {
            ActionType.SHOW_TOAST -> ShowToastProcessor()
            // Other action types will be added here
            else -> throw IllegalArgumentException("Unsupported action type: ${action.actionType}")
        }
    }
}

/** Action executor - executes action flows */
class ActionExecutor(
        private val processorFactory: ActionProcessorFactory = ActionProcessorFactory()
) {
    /** Execute an action flow */
    fun execute(
            context: Context,
            actionFlow: ActionFlow,
            scopeContext: ScopeContext?,
            scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    ) {
        scope.launch {
            // Execute each action sequentially
            for (action in actionFlow.actions) {
                // Generate action ID
                action.actionId = ActionId(java.util.UUID.randomUUID().toString())

                // Check if action is disabled
                val disabled = action.disableActionIf.evaluate(scopeContext) ?: false
                if (disabled) {
                    continue
                }

                // Get processor and execute
                try {
                    @Suppress("UNCHECKED_CAST")
                    val processor = processorFactory.getProcessor(action) as ActionProcessor<Action>
                    processor.execute(
                            context = context,
                            action = action,
                            scopeContext = scopeContext,
                            id = action.actionId!!.id
                    )
                } catch (e: Exception) {
                    // Log error (in production, use proper logging)
                    println("Error executing action: ${e.message}")
                }
            }
        }
    }
}
