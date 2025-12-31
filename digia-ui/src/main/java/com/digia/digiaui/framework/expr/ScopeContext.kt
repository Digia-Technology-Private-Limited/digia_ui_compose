package com.digia.digiaui.framework.expr

import com.digia.digiaui.framework.models.ExprOr

/**
 * Scope context for evaluating expressions Contains variables and functions available during
 * rendering
 */
interface ScopeContext {
    fun getValue(key: String): Any?
    fun hasValue(key: String): Boolean
    fun getAll(): Map<String, Any?>
}

/** Default scope context implementation */
class DefaultScopeContext(private val values: Map<String, Any?> = emptyMap()) : ScopeContext {
    override fun getValue(key: String): Any? {
        return values[key]
    }

    override fun hasValue(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun getAll(): Map<String, Any?> {
        return values
    }
}

/** Extension function to evaluate ExprOr values */
fun <T> ExprOr<T>?.evaluate(scopeContext: ScopeContext?): T? {
    return when (this) {
        is ExprOr.Literal -> this.value
        is ExprOr.Expression -> {
            // Simple expression evaluation - just variable substitution for now
            // In production, this would use a proper expression parser
            val expr = this.expr.trim()
            scopeContext?.getValue(expr) as? T
        }
        null -> null
    }
}
