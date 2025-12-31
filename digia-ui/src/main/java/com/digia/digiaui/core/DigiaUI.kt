package com.digia.digiaui.core

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.digia.digiaui.config.model.DUIConfig

/**
 * CompositionLocal for providing DigiaUIViewModel down the tree (similar to InheritedWidget in
 * Flutter)
 */
val LocalDigiaViewModel =
        compositionLocalOf<DigiaUIViewModel> { error("No DigiaUIViewModel provided") }

// Old DigiaUI composables removed - use DigiaUIApp or DigiaUIAppBuilder instead

/**
 * Main composable for the Digia UI system. This is a simplified entry point for rendering UI when
 * the configuration is already loaded.
 *
 * For apps that need initialization handling, use [com.digia.digiaui.app.DigiaUIApp] or
 * [com.digia.digiaui.app.DigiaUIAppBuilder] instead.
 *
 * @param config The loaded application configuration
 * @param viewModel The DigiaUIViewModel managing the app state
 */
@Composable
fun DigiaUIContent(config: DUIConfig, viewModel: DigiaUIViewModel) {
    val navController = rememberNavController()

    MaterialTheme {
        CompositionLocalProvider(LocalDigiaViewModel provides viewModel) {
            Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
            ) {
                NavHost(navController = navController, startDestination = config.initialRoute) {
                    // Build navigation graph from pages
                    config.pages.keys.forEach { pageId ->
                        composable(route = pageId) {
                            // Placeholder for page rendering (widget rendering excluded)
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) { Text("Page: $pageId") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading configuration...")
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
        ) {
            Text(
                    text = "Error Loading Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
