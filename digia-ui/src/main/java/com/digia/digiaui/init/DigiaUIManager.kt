package com.digia.digiaui.init

import com.digia.digiaexpr.callable.ExprCallableImpl
import com.digia.digiaexpr.callable.ExprClass
import com.digia.digiaexpr.callable.ExprClassInstance
import com.digia.digiaui.config.model.DUIConfig
import com.digia.digiaui.framework.logging.Logger
import com.digia.digiaui.framework.models.Variable
import com.digia.digiaui.network.NetworkClient
import com.digia.digiaui.utils.DigiaInspector
import com.digia.digiaui.utils.DigiaUIHost
import kotlin.collections.mapOf

/**
 * Singleton manager for accessing the initialized Digia UI instance.
 *
 * [DigiaUIManager] provides global access to the DigiaUI instance and its core components. This
 * follows the Flutter implementation pattern where DigiaUIManager acts as a central access point
 * for SDK resources.
 *
 * Key responsibilities:
 * - Global access point to DigiaUI instance
 * - Provides access to network client, config, and other core components
 * - Manages SDK lifecycle (initialization and cleanup)
 *
 * Example usage:
 * ```kotlin
 * // Initialize
 * val digiaUI = DigiaUI.initialize(options)
 * DigiaUIManager.initialize(digiaUI)
 *
 * // Access anywhere in the app
 * val accessKey = DigiaUIManager.getInstance().accessKey
 * val networkClient = DigiaUIManager.getInstance().networkClient
 * val inspector = DigiaUIManager.getInstance().inspector
 * ```
 */
class DigiaUIManager private constructor() {

    private var _digiaUI: DigiaUI? = null

    /** The current DigiaUI instance, if initialized */
    val safeInstance: DigiaUI?
        get() = _digiaUI

    /** Gets the DigiaUI instance, throwing if not initialized */
    private val instance: DigiaUI
        get() =
                _digiaUI
                        ?: throw IllegalStateException(
                                "DigiaUI not initialized. Call DigiaUIManager.initialize() first."
                        )

    /** The access key used for authentication */
    val accessKey: String
        get() = instance.initConfig.accessKey

    /** The inspector instance for debugging and network monitoring */
    val inspector: DigiaInspector?
        get() = instance.initConfig.developerConfig?.inspector

    /** Whether the inspector is enabled */
    val isInspectorEnabled: Boolean
        get() = instance.initConfig.developerConfig?.inspector != null

    /** The network client for API communications */
    val networkClient: NetworkClient
        get() = instance.networkClient

    /** The application configuration */
    val config: DUIConfig
        get() = instance.dslConfig

    /** The hosting configuration (Dashboard, Custom, etc.) */
    val host: DigiaUIHost?
        get() = instance.initConfig.developerConfig?.host

    /** Environment variables from the configuration */
    val environmentVariables: Map<String, Variable>
        get() = config.getEnvironmentVariables()

    /**
     * JavaScript variables for expression evaluation.
     *
     * Provides access to JS functions through expression evaluation system.
     * This is a placeholder for future JS integration.
     *
     * In Flutter, this exposes: { 'js': ExprClassInstance(...) }
     * For now, returns empty map until JS evaluation is implemented.
     */
    val jsVars: Map<String, Any>
        get() {
    return      mapOf(
                "js" to ExprClassInstance(
                    ExprClass(
                        name = "js", fields = mutableMapOf(), methods = mapOf(
                            "eval" to ExprCallableImpl(
                            _arity = 2,
                            fn = { evaluator, arguments ->
                                safeInstance?.dslConfig?.jsFunctions?.callJs(
                                    arguments[0].toString(),
                                    arguments
                                        .drop(1)
                                        .map { it }
                                )
                            }
                        )

                    )))
            )
        }


//    fn: (evaluator, arguments) {
//        return safeInstance?.dslConfig.jsFunctions?.callJs(
//            _toValue<String>(evaluator, arguments[0])!,
//            arguments
//                .skip(1)
//                .map((e) => _toValue(evaluator, e))
//        .toList());
//        ),
//        arity: 2

    companion object {
        @Volatile private var INSTANCE: DigiaUIManager? = null

        /** Gets the singleton instance of DigiaUIManager */
        fun getInstance(): DigiaUIManager {
            return INSTANCE
                    ?: synchronized(this) { INSTANCE ?: DigiaUIManager().also { INSTANCE = it } }
        }

        /**
         * Initializes the manager with a DigiaUI instance
         *
         * @param digiaUI The initialized DigiaUI instance
         */
        fun initialize(digiaUI: DigiaUI) {
            Logger.log("DigiaUIManager initialized")
            getInstance()._digiaUI = digiaUI
        }

        /** Destroys the manager and cleans up resources */
        fun destroy() {
            Logger.log("DigiaUIManager destroyed")
            getInstance()._digiaUI = null
        }
    }
}
