package com.digia.digiaui.framework.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.page.ConfigProvider
import com.digia.digiaui.framework.page.DUIPage
import kotlinx.coroutines.flow.collectLatest

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
 */
@Composable
fun DUINavHost(
        configProvider: ConfigProvider,
        startPageId: String,
        startPageArgs: Map<String, Any?>? = null,
        registry: VirtualWidgetRegistry,
        navController: NavHostController = rememberNavController()
) {
    // Store start page args if provided
    LaunchedEffect(startPageArgs) {
        if (startPageArgs != null) {
            NavigationManager.setPageArgs(startPageId, startPageArgs)
        }
    }

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
                            // Pop up to the previous destination
                            popUpTo(
                                    navController.currentDestination?.route
                                            ?: PageRoute(startPageId)
                            ) {
                                inclusive = true
                                saveState = true
                            }
                            restoreState = true
                            //                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(event.route) {
                            restoreState = true
                            //                            launchSingleTop = true
                        }
                    }
                }
                is NavigationEvent.Pop -> {
                    val canPop = navController.previousBackStackEntry != null
                    if (canPop || !event.maybe) {
                        val poppedRoute = navController.currentBackStackEntry?.destination?.route
                        val poppedPageId = poppedRoute?.let { extractPageIdFromRoute(it) }

                        val popped = navController.popBackStack()

                        val returningRoute = navController.currentBackStackEntry?.destination?.route
                        val returningPageId = returningRoute?.let { extractPageIdFromRoute(it) }

                        if (popped && poppedPageId != null && returningPageId != null && event.result != null) {
                            NavigationManager.queuePendingCallback(
                                returningPageId = returningPageId,
                                lookupPageId = poppedPageId,
                                result = event.result
                            )
                        }
                    }
                }
                is NavigationEvent.PopTo -> {
                    navController.popBackStack(route = event.route, inclusive = event.inclusive)
                }
                is NavigationEvent.ExecuteResultCallback -> {
                    // This is handled via Pop events
                }
            }
        }
    }

    // Handle back button
    BackHandler(enabled = navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }

    // Provide NavController to composition tree
    CompositionLocalProvider(LocalDUINavController provides navController) {
        NavHost(navController = navController, startDestination = PageRoute(startPageId)) {
            // Register a single type-safe route pattern that handles ALL pages
            composable<PageRoute> { backStackEntry ->
                // Extract the actual page ID from the type-safe route
                val route = backStackEntry.toRoute<PageRoute>()
                val pageId = route.pageId

                // Get page arguments from NavigationManager
                val pageArgs = remember(pageId) { NavigationManager.getPageArgs(pageId) }

                val pageDef = remember(pageId) { configProvider.getPageDefinition(pageId) }

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

/** Extracts the page ID from a route string. Route format: "PageRoute/{pageId}" */
private fun extractPageIdFromRoute(route: String): String? {
    // For type-safe routes, the pageId is embedded in the route
    // This is a simplified extraction - adjust based on actual route format
    return route.substringAfterLast("/", "").takeIf { it.isNotEmpty() }
}
