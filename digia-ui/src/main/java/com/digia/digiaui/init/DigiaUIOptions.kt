package com.digia.digiaui.init

import android.content.Context
import com.digia.digiaui.analytics.DUIAnalytics
import com.digia.digiaui.network.NetworkConfiguration
import com.digia.digiaui.utils.DeveloperConfig

/**
 * Configuration options for initializing the Digia UI SDK.
 *
 * [DigiaUIOptions] contains all the necessary configuration parameters required to initialize the
 * Digia UI system. This includes authentication, environment settings, flavor configuration, and
 * optional developer settings.
 *
 * Example usage:
 * ```kotlin
 * val options = DigiaUIOptions(
 *     context = applicationContext,
 *     accessKey = "YOUR_ACCESS_KEY",
 *     environment = Environment.Production,
 *     flavor = Flavor.Debug(),
 *     analytics = MyAnalyticsHandler(),
 *     networkConfiguration = NetworkConfiguration.withDefaults()
 * )
 * ```
 *
 * @param context Android application context
 * @param accessKey Your Digia Studio access key for authentication
 * @param environment The deployment environment (Local, Development, Production, or Custom)
 * @param flavor Build flavor configuration (Debug, Staging, Release, or Versioned)
 * @param analytics Optional analytics handler for tracking events
 * @param networkConfiguration Optional network configuration for timeouts and interceptors
 * @param developerConfig Optional developer-specific configuration for debugging
 */
data class DigiaUIOptions(
        val context: Context,
        val accessKey: String,
        val environment: Environment = Environment.Production,
        val flavor: Flavor =
                Flavor.Debug(), // Default to Debug flavor which doesn't require extra params
        val analytics: DUIAnalytics? = null,
        val networkConfiguration: NetworkConfiguration? = null,
        val developerConfig: DeveloperConfig = DeveloperConfig()
)

