package com.digia.digiaui.framework.state

class StateTree {

    private val parentMap = mutableMapOf<StateContext, StateContext?>()
    private val childrenMap = mutableMapOf<StateContext, MutableSet<StateContext>>()

    fun attach(parent: StateContext?, child: StateContext) {
        parentMap[child] = parent
        if (parent != null) {
            childrenMap.getOrPut(parent) { mutableSetOf() }.add(child)
        }
    }

    fun detach(child: StateContext) {
        val parent = parentMap.remove(child)
        parent?.let { childrenMap[it]?.remove(child) }
        childrenMap.remove(child)
    }

    fun parentOf(ctx: StateContext): StateContext? = parentMap[ctx]

    fun childrenOf(ctx: StateContext): Set<StateContext> = childrenMap[ctx].orEmpty()

    /** Resolve key owner upward */
    fun findOwner(start: StateContext, key: String): StateContext? {
        var current: StateContext? = start
        while (current != null) {
            if (current.containsLocal(key)) return current
            current = parentOf(current)
        }
        return null
    }
}
