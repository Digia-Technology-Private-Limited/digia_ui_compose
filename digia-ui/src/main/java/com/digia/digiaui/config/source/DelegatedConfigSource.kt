package com.digia.digiaui.config.source

import com.digia.digiaui.config.source.ConfigSource
import com.digia.digiaui.config.model.DUIConfig

/**
 * ConfigSource that wraps a lambda for custom/complex loading logic
 *
 * This allows for flexible configuration loading strategies without creating a new class for each
 * variant. Used by ConfigStrategyFactory to implement complex release strategies with fallbacks and
 * retries.
 *
 * Example:
 * ```kotlin
 * DelegatedConfigSource {
 *     // Try network first
 *     try {
 *         networkSource.getConfig()
 *     } catch (e: Exception) {
 *         // Fall back to cache
 *         cachedSource.getConfig()
 *     }
 * }
 * ```
 *
 * @param block Suspend lambda that returns DUIConfig
 */
class DelegatedConfigSource(private val block: suspend () -> DUIConfig) : ConfigSource {

    override suspend fun getConfig(): DUIConfig {
        return block()
    }
}
