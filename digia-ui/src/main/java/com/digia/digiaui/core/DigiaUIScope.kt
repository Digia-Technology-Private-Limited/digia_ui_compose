//package com.digia.digiaui.core
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.compositionLocalOf
//import com.digia.digiaui.framework.analytics.DUIAnalytics
//import com.digia.digiaui.framework.message.MessageBus
//
///** CompositionLocal for providing MessageBus down the tree */
//val LocalMessageBus = compositionLocalOf<MessageBus?> { null }
//
///** CompositionLocal for providing Analytics down the tree */
//val LocalAnalytics = compositionLocalOf<DUIAnalytics?> { null }
//
///**
// * Provides access to Digia UI SDK resources through the Compose tree. This composable must be
// * placed above any composables that need access to the SDK features.
// *
// * The [DigiaUIScope] manages a [MessageBus] instance that enables communication between different
// * parts of the application. Similar to Flutter's InheritedWidget pattern.
// *
// * @param messageBus The message bus instance used for communication within the SDK
// * @param analytics Optional analytics handler for tracking user interactions
// * @param content The child composable content
// */
//@Composable
//fun DigiaUIScope(
//        messageBus: MessageBus = MessageBus(),
//        analytics: DUIAnalytics? = null,
//        content: @Composable () -> Unit
//) {
//    CompositionLocalProvider(
//            LocalMessageBus provides messageBus,
//            LocalAnalytics provides analytics,
//            content = {
//                content()
//            }
//    )
//}
//
///**
// * Extension function to get MessageBus from current composition Throws if not found in the
// * composition tree
// */
//@Composable
//fun requireMessageBus(): MessageBus {
//    return LocalMessageBus.current
//            ?: throw IllegalStateException(
//                    "No MessageBus found in composition. Wrap your content with DigiaUIScope."
//            )
//}
//
///** Extension function to get Analytics from current composition Returns null if not configured */
//@Composable
//fun getAnalytics(): DUIAnalytics? {
//    return LocalAnalytics.current
//}
