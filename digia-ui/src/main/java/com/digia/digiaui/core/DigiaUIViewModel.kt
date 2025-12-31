package com.digia.digiaui.core

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.digia.digiaui.analytics.DUIAnalytics
import com.digia.digiaui.api.ApiClient
import com.digia.digiaui.config.model.DUIConfig
import com.digia.digiaui.framework.data.DataSourceManager
import com.digia.digiaui.framework.expression.ExpressionEvaluator
import com.digia.digiaui.framework.logging.Logger
import com.digia.digiaui.framework.message.MessageBus
import com.digia.digiaui.framework.page.PageManager
import com.digia.digiaui.framework.preferences.PreferencesStore
import com.digia.digiaui.framework.resource.ResourceProvider
import com.digia.digiaui.framework.validation.ValidationManager
import com.digia.digiaui.init.Environment
import com.digia.digiaui.state.StateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core ViewModel for the Digia UI system. Manages the entire application state and lifecycle.
 *
 * This ViewModel provides:
 * - Configuration management and loading
 * - Access to all core managers (page, data, state, validation, etc.)
 * - Reactive state management using Kotlin Flow
 * - Lifecycle-aware resource management
 *
 * Example usage:
 * ```kotlin
 * val viewModel = DigiaUIViewModel(
 *     context = applicationContext,
 *     config = appConfig,
 *     environment = Environment.Production,
 *     analytics = MyAnalytics()
 * )
 *
 * // Observe configuration state
 * val configState by viewModel.configState.collectAsState()
 * when (configState) {
 *     is ConfigState.Success -> // Show UI
 *     is ConfigState.Error -> // Show error
 * }
 * ```
 */
class DigiaUIViewModel(
        private val context: Context,
        val config: DUIConfig,
        private val environment: Environment,
        val analytics: DUIAnalytics?
) : ViewModel() {

    // Core managers and services
    val messageBus = MessageBus()
    val resourceProvider = ResourceProvider()
    val preferencesStore = PreferencesStore.getInstance()
    val stateManager = StateManager()
    val expressionEvaluator = ExpressionEvaluator()
    val validationManager = ValidationManager()

    // Configuration state - already loaded, so set to Success
    private val _configState = MutableStateFlow<ConfigState>(ConfigState.Success(config))
    val configState: StateFlow<ConfigState> = _configState.asStateFlow()

    // Managers initialized with config
    val pageManager: PageManager = PageManager()
    val dataSourceManager: DataSourceManager = DataSourceManager()
    val apiClient: ApiClient = ApiClient(config = config, baseUrl = getBaseUrl(), timeout = 30)

    init {
        Logger.log("DigiaUIViewModel initialized")

        // TODO: Initialize global app state
        // DUIAppState.init(config.appState ?: emptyList())
    }

    /** Gets the base URL for API requests based on environment */
    private fun getBaseUrl(): String {
        return when (environment) {
            is Environment.Local -> "http://localhost:3000"
            is Environment.Development -> "https://dev-api.digiastudio.com"
            is Environment.Production -> "https://api.digiastudio.com"
            is Environment.Custom -> (environment as Environment.Custom).apiBaseUrl
        }
    }

    /** Reloads the configuration (placeholder for future implementation) */
    fun reloadConfiguration() {
        // TODO: Implement configuration reloading
        Logger.log("Configuration reload requested")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.log("DigiaUIViewModel cleared")
    }
}

/** Configuration state sealed class for reactive UI updates */
sealed class ConfigState {
    /** Configuration is being loaded */
    object Loading : ConfigState()

    /**
     * Configuration loaded successfully
     *
     * @param config The loaded application configuration
     */
    data class Success(val config: DUIConfig) : ConfigState()

    /**
     * Configuration loading failed
     *
     * @param message Error message describing the failure
     */
    data class Error(val message: String) : ConfigState()
}

/**
 * Composable function to create and remember a DigiaUIViewModel instance
 *
 * @param context Android application context
 * @param config The application configuration
 * @param environment The deployment environment
 * @param analytics Optional analytics handler
 * @return A remembered DigiaUIViewModel instance
 */
@Composable
fun rememberDigiaUIViewModel(
        context: Context,
        config: DUIConfig,
        environment: Environment,
        analytics: DUIAnalytics?
): DigiaUIViewModel {
    return remember(context, config, environment, analytics) {
        DigiaUIViewModel(context, config, environment, analytics)
    }
}
