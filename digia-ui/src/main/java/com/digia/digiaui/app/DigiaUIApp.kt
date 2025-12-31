//package com.digia.digiaui.app
//
//import androidx.compose.runtime.*
//import com.digia.digiaui.analytics.DUIAnalytics
//import com.digia.digiaui.core.DigiaUIScope
//import com.digia.digiaui.core.DigiaUIViewModel
//import com.digia.digiaui.framework.DUIFactory
//import com.digia.digiaui.framework.logging.Logger
//import com.digia.digiaui.framework.message.MessageBus
//import com.digia.digiaui.init.DigiaUI
//import com.digia.digiaui.init.DigiaUIManager
//
///**
// * The main application wrapper for integrating Digia UI SDK into Android Compose applications.
// *
// * [DigiaUIApp] serves as the root composable that manages the lifecycle of the Digia UI system and
// * provides a scope for analytics, messaging, and other core functionalities.
// *
// * This composable handles:
// * - Initialization and disposal of the Digia UI system
// * - Global app state management
// * - UI factory setup with custom resources
// * - Analytics and message bus integration
// * - Environment variable configuration
// * - Providing Digia UI context to child composables
// *
// * Example usage:
// * ```kotlin
// * DigiaUIApp(
// *     digiaUI = digiaUI,
// *     viewModel = viewModel,
// *     analytics = MyAnalyticsHandler(),
// *     messageBus = MyMessageBus(),
// *     environmentVariables = mapOf("authToken" to "1234567890")
// * ) {
// *     // Your app content here
// *     DUIFactory.getInstance().createInitialPage()
// * }
// * ```
// *
// * @param digiaUI The initialized DigiaUI instance containing configuration and resources
// * @param viewModel The DigiaUIViewModel managing the app state
// * @param analytics Optional analytics handler for tracking user interactions and events
// * @param messageBus Optional message bus for inter-component communication
// * @param environmentVariables Environment variables to make available in expressions and
// * configurations
// * @param content Builder function that creates the child composable tree
// */
//@Composable
//fun DigiaUIApp(
//        digiaUI: DigiaUI,
//        viewModel: DigiaUIViewModel,
//        analytics: DUIAnalytics? = null,
//        messageBus: MessageBus = MessageBus(),
//        environmentVariables: Map<String, Any?>? = null,
//        content: @Composable () -> Unit
//) {
//    // Initialize on first composition
//    DisposableEffect(digiaUI) {
//        // Initialize the Digia UI manager with the provided configuration
//        DigiaUIManager.initialize(digiaUI)
//
//        // Initialize global app state with configuration from DSL
//        // DUIAppState.init(digiaUI.config.appState ?: emptyList())
//
//        // Set up the UI factory with custom resources and providers
//        DUIFactory.getInstance()
//                .initialize(
//                        config = digiaUI.config,
//                        // Additional configuration can be added here
//                        )
//
//        // Apply environment variables if provided
//        environmentVariables?.let { DUIFactory.getInstance().setEnvironmentVariables(it) }
//
//        Logger.log("DigiaUIApp initialized")
//
//        onDispose {
//            // Clean up all Digia UI resources when disposed
//            DigiaUIManager.destroy()
//            DUIFactory.getInstance().destroy()
//            Logger.log("DigiaUIApp disposed")
//        }
//    }
//
//    // Wrap the child composable tree with DigiaUIScope to provide
//    // analytics and message bus context to all descendant composables
//    DigiaUIScope(
//            viewModel = viewModel,
//            messageBus = messageBus,
//            analytics = analytics,
//            content = content
//    )
//}
