package com.digia.digiaui.framework.actions.base

import android.content.Context
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.state.StateContext

/** Action processor base class */
abstract class ActionProcessor<T : Action> {
    /** Execute the action */
    abstract  fun execute(
        context: Context,
        action: T,
        scopeContext: ScopeContext?,
        stateContext: StateContext?,
        id: String
    ): Any?
}
