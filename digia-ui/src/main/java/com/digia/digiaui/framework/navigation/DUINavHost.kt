package com.digia.digiaui.framework.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.actions.LocalActionExecutor
import com.digia.digiaui.framework.page.ConfigProvider
import com.digia.digiaui.framework.page.DUIPage
import com.digia.digiaui.framework.page.RootStateTreeProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Navigation controller composition local for providing access throughout the tree */
val LocalDUINavController =
        staticCompositionLocalOf<NavHostController> { error("NavController not provided") }

/**
 * DUINavHost - Navigation host component using official Compose Navigation
 *
 * Manages page navigation and routing based on the DUIConfig using androidx.navigation.compose.
 * Integrates with NavigationManager to bridge existing navigation events to Compose Navigation.
 *
 * @param configProvider The configuration provider containing page definitions
 * @param startPageId The initial page to display
 * @param startPageArgs Optional arguments for the start page
 * @param registry The widget registry for creating widgets
 * @param navController Optional custom NavController (creates one if not provided)
 * @param onNavigationComplete Optional callback when navigation completes (for SDK Activity mode)
 */
@Composable
fun DUINavHost(
        configProvider: ConfigProvider,
        startPageId: String,
        startPageArgs: Map<String, Any?>? = null,
        registry: VirtualWidgetRegistry,
        navController: NavHostController = rememberNavController(),
        onNavigationComplete: ((Map<String, Any?>?) -> Unit)? = null
) {
    // Store start page args if provided
    LaunchedEffect(startPageArgs) {
        if (startPageArgs != null) {
            NavigationManager.setPageArgs(startPageId, startPageArgs)
        }
    }

    // Scope for executing callbacks
    val scope = rememberCoroutineScope()

    // Capture action executor for ExecuteResultCallback
    val actionExecutor = LocalActionExecutor.current

    // Listen to navigation events from NavigationManager
    LaunchedEffect(navController) {
        NavigationManager.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.Navigate -> {
                    // Store arguments for the new page
                    if (event.args != null) {
                        NavigationManager.setPageArgs(event.route.pageId, event.args)
                    }

                    // Navigate using type-safe route
                    if (event.replace) {
                        navController.navigate(event.route) {
                            // Pop up to the previous destination and replace it
                            popUpTo(
                                    navController.currentDestination?.route
                                            ?: PageRoute(startPageId)
                            ) {
                                inclusive = true
                                saveState = true
                            }
                        }
                    } else {
                        // Normal forward navigation - back stack maintains state automatically
                        navController.navigate(event.route) { restoreState = true }
                    }
                }
                is NavigationEvent.Pop -> {
                    if (navController.previousBackStackEntry != null || !event.maybe) {
                        // Get the previous route for result callback
                        val previousRoute = navController.previousBackStackEntry?.destination?.route

                        // Pop the stack
                        val popped = navController.popBackStack()

                        // Execute result callback if registered and pop was successful
                        if (popped && previousRoute != null && event.result != null) {
                            // Extract page ID from previousRoute
                            val pageId = extractPageIdFromRoute(previousRoute)
                            if (pageId != null) {
                                NavigationManager.executeResultCallback(pageId, event.result)
                            }
                        }
                    }
                }
                is NavigationEvent.PopTo -> {
                    navController.popBackStack(route = event.route, inclusive = event.inclusive)
                }
                is NavigationEvent.ExecuteResultCallback -> {
                    // Execute the result callback action flow
                    scope.launch {
                        try {
                            // Create scope context with result data
                            // The event contains the complete execution context from where
                            // the callback was registered, preserving the state hierarchy
                            val resultScopeContext =
                                    event.scopeContext?.copyAndExtend(
                                            mapOf("result" to event.result)
                                    )
                                            ?: event.scopeContext

                            // Execute using the original execution context
                            // This preserves State1 -> DUINavHost -> State2 -> Button hierarchy
                            actionExecutor.execute(
                                    context = navController.context,
                                    actionFlow = event.actionFlow,
                                    scopeContext = resultScopeContext,
                                    stateContext = event.stateContext,
                                    resourcesProvider = event.resourcesProvider,
                                    scope = this
                            )
                        } catch (e: Exception) {
                            println("Error executing result callback: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // Handle back button
    BackHandler(
            enabled = navController.previousBackStackEntry != null || onNavigationComplete != null
    ) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            // No more pages to pop - complete navigation
            onNavigationComplete?.invoke(null)
        }
    }

    // SaveableStateHolder for preserving UI state across navigation
    val saveableStateHolder = rememberSaveableStateHolder()

    // Provide NavController to composition tree
    CompositionLocalProvider(LocalDUINavController provides navController) {
        // Provide a single StateTree for the entire navigation graph
        RootStateTreeProvider {
            NavHost(navController = navController, startDestination = PageRoute(startPageId)) {
                // Register a single type-safe route pattern that handles ALL pages
                composable<PageRoute> { backStackEntry ->
                    // Extract the actual page ID from the type-safe route
                    val route = backStackEntry.toRoute<PageRoute>()
                    val pageId = route.pageId

                    // Get page arguments from NavigationManager - use backStackEntry as key
                    val pageArgs =
                            remember(backStackEntry) { NavigationManager.getPageArgs(pageId) }

                    val pageDef =
                            remember(backStackEntry) { configProvider.getPageDefinition(pageId) }

                    // Wrap page in SaveableStateProvider to preserve UI state across navigation
                    saveableStateHolder.SaveableStateProvider(pageId) {
                        // All pages render through the same DUIPage composable
                        DUIPage(
                                pageId = pageId,
                                pageArgs = pageArgs,
                                pageDef = pageDef,
                                registry = registry
                        )
                    }
                }
            }
        }
    }
}

/** Extracts the page ID from a route string. Route format: "PageRoute/{pageId}" */
private fun extractPageIdFromRoute(route: String): String? {
    // For type-safe routes, the pageId is embedded in the route
    // This is a simplified extraction - adjust based on actual route format
    return route.substringAfterLast("/", "").takeIf { it.isNotEmpty() }
}
