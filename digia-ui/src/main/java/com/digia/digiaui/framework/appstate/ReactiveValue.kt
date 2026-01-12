package com.digia.digiaui.framework.appstate

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * A reactive value that holds a value and exposes its changes
 */
open class ReactiveValue<T>(
    initialValue: T,
    val streamName: String
) {

    /** The current value */
    private var _value: T = initialValue



    /** Internal flow for value changes (broadcast) */
    private val _flow = MutableSharedFlow<T>(
        replay = 1,               // last value available to new collectors
        extraBufferCapacity = 1
    )

    /** Public read-only flow */
    val flow: SharedFlow<T> = _flow.asSharedFlow()

    init {
        // Emit initial value (like StreamController initial state)
        _flow.tryEmit(_value)
    }

    /** Get the current value */
    val value: T
        get() = _value

    /**
     * Update the value and notify listeners
     * Returns true if the value was actually changed
     */
    open fun update(newValue: T): Boolean {
        if (_value != newValue) {
            _value = newValue
            _flow.tryEmit(_value)
            return true
        }
        return false
    }

}
