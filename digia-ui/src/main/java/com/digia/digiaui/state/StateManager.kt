//package com.digia.digiaui.state
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//
///**
// * Global application state manager. Manages runtime state that can be accessed and modified
// * throughout the app.
// */
//class StateManager {
//    private val _state = MutableStateFlow<Map<String, Any?>>(emptyMap())
//    val state: StateFlow<Map<String, Any?>> = _state.asStateFlow()
//
//    private val stateMap = mutableMapOf<String, Any?>()
//
//    fun <T> setState(key: String, value: T) {
//        stateMap[key] = value
//        _state.value = stateMap.toMap()
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    fun <T> getState(key: String): T? {
//        return stateMap[key] as? T
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    fun <T> getState(key: String, defaultValue: T): T {
//        return (stateMap[key] as? T) ?: defaultValue
//    }
//
//    fun removeState(key: String) {
//        stateMap.remove(key)
//        _state.value = stateMap.toMap()
//    }
//
//    fun clearState() {
//        stateMap.clear()
//        _state.value = emptyMap()
//    }
//
//    fun hasState(key: String): Boolean {
//        return stateMap.containsKey(key)
//    }
//
//    fun getAllKeys(): Set<String> {
//        return stateMap.keys.toSet()
//    }
//
//    fun updateState(updates: Map<String, Any?>) {
//        stateMap.putAll(updates)
//        _state.value = stateMap.toMap()
//    }
//}
