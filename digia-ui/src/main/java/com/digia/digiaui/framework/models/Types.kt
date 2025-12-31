package com.digia.digiaui.framework.models

/** Core type definitions for DigiaUI */
typealias JsonLike = Map<String, Any?>

/** Expression wrapper - can be either a literal value or an expression string */
sealed class ExprOr<out T> {
    data class Literal<T>(val value: T) : ExprOr<T>()
    data class Expression(val expr: String) : ExprOr<Nothing>()

    companion object {
        fun <T> fromValue(value: Any?): ExprOr<T>? {
            return when (value) {
                null -> null
                is String ->
                        if (value.startsWith("@{") && value.endsWith("}")) {
                            Expression(value.substring(2, value.length - 1))
                        } else {
                            @Suppress("UNCHECKED_CAST") Literal(value as T)
                        }
                else -> {
                    @Suppress("UNCHECKED_CAST") Literal(value as T)
                }
            }
        }
    }
}
