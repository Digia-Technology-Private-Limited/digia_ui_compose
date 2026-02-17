package com.digia.digiaui.framework.navigation

import LocalUIResources
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.actions.LocalActionExecutor
import com.digia.digiaui.framework.page.ConfigProvider
import com.digia.digiaui.framework.page.DUIPage
import com.digia.digiaui.framework.state.StateTree
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.builtins.serializer
import java.util.UUID

/**
 * Navigation Configuration - Represents screens in the navigation stack
 * 
 * Decompose uses Parcelable configurations for state preservation across process death.
 * Args are stored separately in memory since Map<String, Any?> is not Parcelable.
 */
@Parcelize data class ScreenConfig( val pageId: String, val timestamp: Long = System.currentTimeMillis() ) : Parcelable

/**
 * DUI Root Component - Manages navigation stack using Decompose
 *
 * This component holds the navigation logic and state for the entire app.
 * Decompose automatically preserves state and handles lifecycle.
 */
class DUIRootComponent(
    componentContext: ComponentContext,
    initialPageId: String,
    initialArgs: Map<String, Any?>? = null
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfig>()
    private val argsStore = mutableMapOf<String, Map<String, Any?>?>()
    private val stateTreeStore = mutableMapOf<String, StateTree>()
    
    val childStack =
        childStack(
            source = navigation,
//            serializer = ScreenConfig.serializer(),
            initialConfiguration = ScreenConfig(pageId = initialPageId).also { argsStore[it.pageId + "_" + it.timestamp] = initialArgs },
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild( config: ScreenConfig, componentContext: ComponentContext ): ScreenChild { val argsKey = config.pageId + "_" + config.timestamp
        return ScreenChild( pageId = config.pageId, args = argsStore[argsKey], componentContext = componentContext ) }

    fun navigate(pageId: String, args: Map<String, Any?>?, replace: Boolean) {
        val config = ScreenConfig(pageId = pageId)
        val argsKey = config.pageId + "_" + config.timestamp
        if (args != null) { argsStore[argsKey] = args }
        if (replace) navigation.replaceCurrent(config)
        else navigation.push(config)
    }

    fun pop(result: Any?) {
        val active = childStack.value.active.instance
        if (result != null) {
            NavigationManager.executeResultCallback(active.pageId, result)
        }
        navigation.pop()
    }

    fun popTo(pageId: String, inclusive: Boolean) {
        val stack = childStack.value.items
        val target = stack.lastOrNull { it.configuration.pageId == pageId }
        if (target != null) {
//            navigation.popTo(target.configuration)
            if (inclusive) navigation.pop()
        }
    }

    val canPop get() = childStack.value.items.size > 1

    fun getOrCreateStateTree(pageId: String, timestamp: Long): StateTree {
        val key = pageId + "_" + timestamp
        return stateTreeStore.getOrPut(key) { StateTree() }
    }


    data class ScreenChild(
        val pageId: String,
        val args: Map<String, Any?>?,
        val componentContext: ComponentContext
    )
}

/**
 * DUINavController - Wrapper for compatibility with existing code
 */
class DUINavController(private val rootComponent: DUIRootComponent) {
    
    fun navigate(pageId: String, args: Map<String, Any?>? = null, replace: Boolean = false) {
        rootComponent.navigate(pageId, args, replace)
    }
    
    fun pop(result: Any? = null, maybe: Boolean = true) {
        if (!maybe || rootComponent.canPop) {
            rootComponent.pop(result)
        }
    }
    
    fun popTo(pageId: String, inclusive: Boolean = false) {
        rootComponent.popTo(pageId, inclusive)
    }
    
    val canPop: Boolean
        get() = rootComponent.canPop
}

/** CompositionLocal providers */
val LocalDUINavController = staticCompositionLocalOf<DUINavController> { 
    error("DUINavController not provided") 
}

val LocalDUIConfigProvider = staticCompositionLocalOf<ConfigProvider> { 
    error("ConfigProvider not provided") 
}

val LocalDUIRegistry = staticCompositionLocalOf<VirtualWidgetRegistry> { 
    error("VirtualWidgetRegistry not provided") 
}

val LocalDUIRootComponent = staticCompositionLocalOf<DUIRootComponent?> { 
    null
}

val LocalCurrentScreenConfig = staticCompositionLocalOf<ScreenConfig?> { 
    null
}

/**
 * DUINavHost - Main navigation host using Decompose
 *
 * Sets up Decompose's component-based navigation with automatic state preservation.
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
    startPageArgs: Map<String, Any?>?,
    registry: VirtualWidgetRegistry
) {
    val rootComponent =
        rememberDUIRootComponent(startPageId, startPageArgs)

    val actionExecutor = LocalActionExecutor.current
    val context = LocalContext.current
    val resource= LocalUIResources.current

    val navController = remember(rootComponent) {
        DUINavController(rootComponent)
    }

    LaunchedEffect(Unit) {
        NavigationManager.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Navigate ->
                    navController.navigate(
                        event.route.pageId,
                        event.args,
                        event.replace
                    )

                is NavigationEvent.Pop ->
                    navController.pop(event.result, event.maybe)

                is NavigationEvent.PopTo ->
                    navController.popTo(
                        event.route.pageId,
                        event.inclusive
                    )

                is NavigationEvent.ExecuteResultCallback -> {

                    actionExecutor.execute(context, event.actionFlow, event.scopeContext, event.stateContext, resource)
                }

                else -> {}
            }
        }
    }

    CompositionLocalProvider(
        LocalDUINavController provides navController,
        LocalDUIConfigProvider provides configProvider,
        LocalDUIRegistry provides registry,
        LocalDUIRootComponent provides rootComponent
    ) {
        DUIDecomposeContent(rootComponent)
    }
}

@Composable
private fun DUIDecomposeContent(root: DUIRootComponent) {
    val configProvider = LocalDUIConfigProvider.current
    val registry = LocalDUIRegistry.current

    Children(
        stack = root.childStack,
        animation = stackAnimation(fade())
    ) { child ->
        val instance = child.instance
        val screenConfig = child.configuration

        val pageDef = remember(instance.pageId) {
            configProvider.getPageDefinition(instance.pageId)
        }

        val stateTree = remember(screenConfig.pageId, screenConfig.timestamp) {
            root.getOrCreateStateTree(screenConfig.pageId, screenConfig.timestamp)
        }

        CompositionLocalProvider(
            LocalCurrentScreenConfig provides screenConfig
        ) {
            DUIPage(
                pageId = instance.pageId,
                pageArgs = instance.args,
                pageDef = pageDef,
                registry = registry,
                stateTree = stateTree
            )
        }
    }
}

@Composable
fun rememberDUIRootComponent(
    startPageId: String,
    startArgs: Map<String, Any?>?
): DUIRootComponent {

    val lifecycleOwner = LocalLifecycleOwner.current

    return remember {
        val lifecycle = LifecycleRegistry()

        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> lifecycle.onCreate()
                    Lifecycle.Event.ON_START -> lifecycle.onStart()
                    Lifecycle.Event.ON_RESUME -> lifecycle.onResume()
                    Lifecycle.Event.ON_PAUSE -> lifecycle.onPause()
                    Lifecycle.Event.ON_STOP -> lifecycle.onStop()
                    Lifecycle.Event.ON_DESTROY -> lifecycle.onDestroy()
                    else -> {}
                }
            }
        )

        DUIRootComponent(
            DefaultComponentContext(lifecycle = lifecycle),
            startPageId,
            startArgs
        )
    }
}
