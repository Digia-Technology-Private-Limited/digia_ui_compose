package com.digia.digiaui.sdk.internal

import com.digia.digiaui.framework.appstate.DUIAppState
import com.digia.digiaui.sdk.api.DigiaAppState

class DefaultDigiaAppState : DigiaAppState {
    override fun get(key: String): Any? = DUIAppState.instance.getValue<Any?>(key)

    override fun all(): Map<String, Any?> =
        DUIAppState.instance.all().mapValues { (_, reactive) -> reactive.value }

    override fun set(key: String, value: Any?): Boolean = DUIAppState.instance.update(key, value)

    override fun setAll(values: Map<String, Any?>): Boolean {
        var changed = false
        for ((k, v) in values) {
            changed = DUIAppState.instance.update(k, v) || changed
        }
        return changed
    }
}
