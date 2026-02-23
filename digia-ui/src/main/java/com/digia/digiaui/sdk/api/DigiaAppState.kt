package com.digia.digiaui.sdk.api

/** Only global app-state operations. */
interface DigiaAppState {
    fun get(key: String): Any?
    fun all(): Map<String, Any?>
    fun set(key: String, value: Any?): Boolean
    fun setAll(values: Map<String, Any?>): Boolean
}
