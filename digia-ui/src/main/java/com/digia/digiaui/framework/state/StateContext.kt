package com.digia.digiaui.framework.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshots.Snapshot

class StateContext(
    val namespace: String? = null,
    private val tree: StateTree,
    initialState: Map<String, Any?> = emptyMap()
) {

    /* ---------------- State ---------------- */

    private val values = mutableMapOf<String, Any?>()
    private val scopeVersion = mutableIntStateOf(0)
    private var dirty = false

    /* ---------------- Tracking ---------------- */

    private var isTracking = false
    private var readScopes: MutableSet<String>? = null

    init {
        values.putAll(initialState)
    }

    fun dispose() {
        tree.childrenOf(this).forEach { it.dispose() }
        tree.detach(this)
    }

    /* ---------------- Tracking ---------------- */

    fun startTracking() {
        isTracking = true
        readScopes = mutableSetOf()
    }

    fun stopTracking(): Set<String> {
        isTracking = false
        return readScopes.orEmpty().also { readScopes = null }
    }

    private fun markRead(owner: StateContext) {
        if (!isTracking) return
        owner.namespace?.let { readScopes?.add(it) }
    }

    /* ---------------- Reads ---------------- */

    fun get(key: String): Any? {
        val owner = tree.findOwner(this, key) ?: return null
        markRead(owner)
        return owner.values[key]
    }

    fun observe(stateName: String) {
        if (stateName == namespace) {
            scopeVersion.intValue // 👈 subscription point
            return
        }
        tree.parentOf(this)?.observe(stateName)
    }

    /* ---------------- Writes ---------------- */

    fun set(key: String, value: Any?, notify: Boolean = true) {
        values[key] = value
        if (notify) flush()
        else dirty = true
    }

    /* ---------------- Flush ---------------- */

     fun flush() {
        Snapshot.withMutableSnapshot {
            scopeVersion.intValue++
        }
        dirty = false

        tree.childrenOf(this).forEach { it.flushFromParent() }
    }

    private fun flushFromParent() {
        Snapshot.withMutableSnapshot {
            scopeVersion.intValue++
        }
        dirty = false

        tree.childrenOf(this).forEach { it.flushFromParent() }
    }

    /* ---------------- Utilities ---------------- */

    fun snapshot(): Map<String, Any?> {
        markRead(this)
        return values.toMap()
    }

    fun containsKey(key: String): Boolean =
        tree.findOwner(this, key) != null

    fun containsLocal(key: String): Boolean =
        values.containsKey(key)

    fun debugVersion(): Int = scopeVersion.intValue

}


