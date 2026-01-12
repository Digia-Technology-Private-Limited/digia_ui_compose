package com.digia.digiaui.framework.navigation

import com.digia.digiaui.framework.actions.base.ActionFlow
import com.digia.digiaui.framework.expr.ScopeContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable

/**
 * Serializable route for navigation
 */
@Serializable
data class PageRoute(val pageId: String)

/**
 * Navigation Manager
 *
 * Central manager for handling navigation actions in the Digia UI framework.
 * Uses Kotlin Flow to communicate navigation events from actions to the NavHost.
 */
object NavigationManager {
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 10)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // Store page arguments globally to preserve state across navigation
    private val pageArgsStore = mutableMapOf<String, Map<String, Any?>?>()

    // Store result callbacks for pages that wait for results
    private val resultCallbacks = mutableMapOf<String, ResultCallback>()

    /**
     * Request navigation to a specific page
     */
    fun navigate(pageId: String, args: Map<String, Any?>? = null, replace: Boolean = false) {
        val route = PageRoute(pageId)
        // Store page arguments globally
        if (args != null) {
            pageArgsStore[pageId] = args
        }
        _navigationEvents.tryEmit(NavigationEvent.Navigate(route, args, replace))
    }

    /**
     * Get page arguments for a specific page (single-use: clears after reading)
     */
    fun getPageArgs(pageId: String): Map<String, Any?>? {
        return pageArgsStore.remove(pageId)
    }

    /**
     * Clear page arguments for a specific page
     */
    fun clearPageArgs(pageId: String) {
        pageArgsStore.remove(pageId)
    }

    /**
     * Request to pop the current page
     */
    fun pop(result: Any? = null) {
        _navigationEvents.tryEmit(NavigationEvent.Pop(result))
    }

    /**
     * Request to pop to a specific page
     */
    fun popTo(pageId: String, inclusive: Boolean = false) {
        val route = PageRoute(pageId)
        _navigationEvents.tryEmit(NavigationEvent.PopTo(route, inclusive))
    }

    /**
     * Register a callback to be executed when navigation returns with a result
     */
    fun registerResultCallback(
        pageId: String,
        onResult: ActionFlow,
        scopeContext: ScopeContext?
    ) {
        resultCallbacks[pageId] = ResultCallback(onResult, scopeContext)
    }

    /**
     * Execute result callback if one is registered for the given page
     */
    fun executeResultCallback(pageId: String, result: Any?) {
        val callback = resultCallbacks.remove(pageId)
        if (callback != null) {
            // Emit an event to execute the result callback
            _navigationEvents.tryEmit(
                NavigationEvent.ExecuteResultCallback(
                    actionFlow = callback.onResult,
                    result = result,
                    scopeContext = callback.scopeContext
                )
            )
        }
    }

    /**
     * Clear all registered result callbacks
     */
    fun clearResultCallbacks() {
        resultCallbacks.clear()
    }
}

/**
 * Result callback data
 */
private data class ResultCallback(
    val onResult: ActionFlow,
    val scopeContext: ScopeContext?
)

/**
 * Navigation Events
 */
sealed class NavigationEvent {
    data class Navigate(
        val route: PageRoute,
        val args: Map<String, Any?>? = null,
        val replace: Boolean = false
    ) : NavigationEvent()

    data class Pop(val result: Any? = null) : NavigationEvent()

    data class PopTo(
        val route: PageRoute,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    data class ExecuteResultCallback(
        val actionFlow: ActionFlow,
        val result: Any?,
        val scopeContext: ScopeContext?
    ) : NavigationEvent()
}

/**
 * CompositionLocal for NavController
 * Provides NavController throughout the composition tree
 */
//val LocalNavController = compositionLocalOf<NavHostController?> { null }
