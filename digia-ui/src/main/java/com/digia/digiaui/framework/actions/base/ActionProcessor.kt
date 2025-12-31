package com.digia.digiaui.framework.actions.base

import android.content.Context
import com.digia.digiaui.framework.expr.ScopeContext

/** Action processor base class */
abstract class ActionProcessor<T : Action> {
    /** Execute the action */
    abstract suspend fun execute(
            context: Context,
            action: T,
            scopeContext: ScopeContext?,
            id: String
    ): Any?
}
