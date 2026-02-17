package com.digia.digiaui.framework.state

class StateTree {

    private val parentMap = mutableMapOf<StateContext, StateContext?>()
    private val childrenMap = mutableMapOf<StateContext, MutableSet<StateContext>>()
    private val namespaceMap = mutableMapOf<String, StateContext>()

    fun attach(parent: StateContext?, child: StateContext) {
        parentMap[child] = parent
        if (parent != null) {
            childrenMap.getOrPut(parent) { mutableSetOf() }.add(child)
        }
        // Store by namespace for retrieval
        child.namespace?.let { namespaceMap[it] = child }
    }

    fun detach(child: StateContext) {
        val parent = parentMap.remove(child)
        parent?.let { childrenMap[it]?.remove(child) }
        childrenMap.remove(child)
        child.namespace?.let { namespaceMap.remove(it) }
    }

    fun parentOf(ctx: StateContext): StateContext? =
        parentMap[ctx]

    fun childrenOf(ctx: StateContext): Set<StateContext> =
        childrenMap[ctx].orEmpty()

    /**
     * Resolve key owner upward
     */
    fun findOwner(start: StateContext, key: String): StateContext? {
        var current: StateContext? = start
        while (current != null) {
            if (current.containsLocal(key)) return current
            current = parentOf(current)
        }
        return null
    }

    /**
     * Get existing StateContext by namespace, or null if not found
     */
    fun getByNamespace(namespace: String): StateContext? {
        return namespaceMap[namespace]
    }
}
