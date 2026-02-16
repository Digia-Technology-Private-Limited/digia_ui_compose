package com.digia.digiaui.framework.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.models.PageDefinition
import com.digia.digiaui.framework.page.ConfigProvider
import com.digia.digiaui.framework.page.DUIPage

/**
 * DUIScreen - Voyager Screen implementation for Digia UI pages
 *
 * Represents a single page/screen in the navigation stack. Implements Voyager's Screen interface
 * for seamless navigation.
 */
data class DUIScreen(
        val pageId: String,
        val args: Map<String, Any?>? = null,
) : Screen {
    override val key: ScreenKey = "${pageId}_${System.currentTimeMillis()}" // Unique instance key

    // This property lives as long as the object lives in the stack
    // It will NOT be re-fetched when navigating back
    private var cachedPageDef: Any? = null

    @Composable
    override fun Content() {
        val configProvider = LocalDUIConfigProvider.current
        val registry = LocalDUIRegistry.current

        // Only fetch if we don't have it; this survives 'Back' navigation
        val pageDef = remember {
            cachedPageDef ?: configProvider.getPageDefinition(pageId).also { cachedPageDef = it }
        }

        DUIPage(
                pageId = pageId,
                pageArgs = args,
                pageDef = pageDef as PageDefinition,
                registry = registry
        )
    }
}

/**
 * DUINavController - Wrapper around Voyager Navigator for compatibility
 *
 * Provides navigation methods that work with Voyager's Navigator while maintaining the same API as
 * the previous custom implementation.
 */
class DUINavController internal constructor(private val navigator: Navigator) {

    fun navigate(pageId: String, args: Map<String, Any?>? = null, replace: Boolean = false) {
        val screen = DUIScreen(pageId, args)
        if (replace) {
            navigator.replace(screen)
        } else {
            navigator.push(screen)
        }
    }

    fun pop(result: Any? = null, maybe: Boolean = true) {
        if (!maybe || navigator.canPop) {
            // Get the current screen BEFORE popping to avoid lifecycle issues
            val currentScreen = navigator.lastItem as? DUIScreen

            // Execute result callback if needed
            if (result != null && currentScreen != null) {
                NavigationManager.executeResultCallback(currentScreen.pageId, result)
            }

            // Pop after handling result
            navigator.pop()
        }
    }

    fun popTo(pageId: String, inclusive: Boolean = false) {
        // Find the screen with matching pageId in the stack
        val screens = navigator.items.toList()
        val targetIndex = screens.indexOfLast { (it as? DUIScreen)?.pageId == pageId }

        if (targetIndex == -1) return

        // Pop until we reach the target
        val itemsToPop =
                if (inclusive) {
                    screens.size - targetIndex
                } else {
                    screens.size - targetIndex - 1
                }

        repeat(itemsToPop) { if (navigator.canPop) navigator.pop() }
    }

    val canPop: Boolean
        get() = navigator.canPop
}

/** CompositionLocal providers for navigation dependencies */
val LocalDUINavController =
        staticCompositionLocalOf<DUINavController> { error("DUINavController not provided") }

val LocalDUIConfigProvider =
        staticCompositionLocalOf<ConfigProvider> { error("ConfigProvider not provided") }

val LocalDUIRegistry =
        staticCompositionLocalOf<VirtualWidgetRegistry> {
            error("VirtualWidgetRegistry not provided")
        }

/**
 * DUINavHost - Main navigation host using Voyager Navigator
 *
 * Sets up Voyager Navigator and bridges it with NavigationManager for server-driven navigation
 * actions.
 *
 * @param configProvider Configuration provider for page definitions
 * @param startPageId Initial page to display
 * @param startPageArgs Optional arguments for the start page
 * @param registry Widget registry for rendering UI components
 */
@Composable
fun DUINavHost(
        configProvider: ConfigProvider,
        startPageId: String,
        startPageArgs: Map<String, Any?>? = null,
        registry: VirtualWidgetRegistry
) {
    val startScreen = remember { DUIScreen(startPageId, startPageArgs) }

    CompositionLocalProvider(
            LocalDUIConfigProvider provides configProvider,
            LocalDUIRegistry provides registry
    ) {
        Navigator(startScreen) { navigator ->
            val navController = remember(navigator) { DUINavController(navigator) }

            // Bridge NavigationManager events to Voyager Navigator
            LaunchedEffect(Unit) {
                NavigationManager.navigationEvents.collect { event ->
                    when (event) {
                        is NavigationEvent.Navigate -> {
                            navController.navigate(
                                    pageId = event.route.pageId,
                                    args = event.args,
                                    replace = event.replace
                            )
                        }
                        is NavigationEvent.Pop -> {
                            navController.pop(event.result, event.maybe)
                        }
                        is NavigationEvent.PopTo -> {
                            navController.popTo(event.route.pageId, event.inclusive)
                        }
                        is NavigationEvent.ExecuteResultCallback -> {
                            // Result callbacks are handled in pop()
                        }
                    }
                }
            }

            CompositionLocalProvider(LocalDUINavController provides navController) {
                // Render only the current screen to avoid lifecycle issues
                CurrentScreen()
            }
        }
    }
}
