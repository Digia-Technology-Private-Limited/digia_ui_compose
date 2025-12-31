//package com.digia.digiaui.framework
//
//import androidx.compose.runtime.Composable
//import com.digia.digiaui.config.ConfigProvider
//import com.digia.digiaui.config.model.DUIConfig
//import com.digia.digiaui.framework.logging.Logger
//import com.digia.digiaui.framework.page.DUIPage
//import com.digia.digiaui.framework.widgets.registerBuiltInWidgets
//import com.digia.digiaui.init.DigiaUIManager
//
///**
// * Central factory class for creating Digia UI widgets, pages, and components.
// *
// * [DUIFactory] is a singleton that serves as the main entry point for creating UI elements from
// * JSON configurations. It manages UI resources, widget registries, and provides methods for
// * creating various UI elements with optional customization.
// *
// * This follows the Flutter DUIFactory pattern, acting as the central point for:
// * - Creating pages and components from JSON definitions
// * - Managing UI resources (icons, images, fonts, colors)
// * - Handling widget registration for custom components
// * - Providing resource override capabilities
// * - Managing navigation routes and bottom sheets
// *
// * Example usage:
// * ```kotlin
// * // Initialize factory
// * DUIFactory.getInstance().initialize(config = appConfig)
// *
// * // Create a page
// * DUIFactory.getInstance().createPage("pageId")
// *
// * // Register custom widget
// * DUIFactory.getInstance().registerWidget("custom/myWidget") { data, parent ->
// *     MyCustomWidget(data)
// * }
// * ```
// */
//class DUIFactory private constructor() {
//
//    private lateinit var configProvider: ConfigProvider
//    private lateinit var widgetRegistry: DefaultVirtualWidgetRegistry
//    private var resources: UIResources = UIResources()
//    private val environmentVariables = mutableMapOf<String, Any?>()
//    private var isInitialized = false
//
//    /**
//     * Initializes the singleton factory with all necessary configuration and resources.
//     *
//     * This method must be called before using any other factory methods. It sets up the widget
//     * registry, configuration provider, and UI resources based on the initialized DigiaUI instance
//     * and optional custom resources.
//     *
//     * @param config The application configuration containing pages, components, and resources
//     * @param customIcons Custom icon mappings to override or extend default icons
//     * @param customImages Custom image provider mappings for app-specific images
//     *
//     * @throws IllegalStateException if DigiaUIManager is not properly initialized
//     */
//    fun initialize(
//            config: DUIConfig,
//            customIcons: Map<String, Any>? = null,
//            customImages: Map<String, Any>? = null
//    ) {
//        if (isInitialized) {
//            Logger.log("DUIFactory already initialized, reinitializing...")
//            destroy()
//        }
//
//        // Ensure DigiaUIManager is properly initialized
//        val digiaUIInstance = DigiaUIManager.getInstance().safeInstance
//        if (digiaUIInstance == null) {
//            throw IllegalStateException(
//                    "DigiaUIManager is not initialized. Make sure to call DigiaUI.initialize() " +
//                            "and DigiaUIManager.initialize() before calling DUIFactory.initialize()."
//            )
//        }
//
//        // Initialize configuration provider with both config and networkClient
//        configProvider = DUIConfigProvider(
//            config,
//            digiaUIInstance.networkClient,
//            digiaUIInstance.initConfig.context
//        )
//
//        // Create UI resources from config
//        resources =
//                UIResources(
//                        colors = config.colorTokens,
//                        darkColors = config.darkColorTokens,
//                        fonts = config.fontTokens,
//                        customIcons = customIcons,
//                        customImages = customImages
//                )
//
//        // Initialize widget registry with component builder
//        widgetRegistry =
//                DefaultVirtualWidgetRegistry(
//                        componentBuilder = { id, args ->
//                            // Component rendering placeholder
//                            // TODO: Implement component rendering
//                        }
//                )
//
//        // Register all built-in widgets
//        widgetRegistry.registerBuiltInWidgets()
//
//        isInitialized = true
//        Logger.log("DUIFactory initialized successfully")
//    }
//
//    /**
//     * Creates a page by ID with optional arguments.
//     *
//     * @param pageId The unique identifier of the page to create
//     * @param pageArgs Optional arguments to pass to the page
//     * @return Composable page content
//     * @throws IllegalArgumentException if page is not found
//     * @throws IllegalStateException if factory is not initialized
//     */
//    @Composable
//    fun CreatePage(pageId: String, pageArgs: Map<String, Any?>? = null) {
//        checkInitialized()
//
//        val pageDef =
//                configProvider.getPageDefinition(pageId)
//                        ?: throw IllegalArgumentException("Page not found: $pageId")
//
//        ResourceProvider(resources) {
//            DUIPage(
//                    pageId = pageId,
//                    pageArgs = pageArgs,
//                    pageDef = pageDef,
//                    registry = widgetRegistry
//            )
//        }
//    }
//
//    /**
//     * Creates the initial page from configuration.
//     *
//     * Uses the initialRoute defined in the configuration to determine which page to display first.
//     *
//     * @return Composable initial page content
//     * @throws IllegalStateException if factory is not initialized or no pages exist
//     */
//    @Composable
//    fun CreateInitialPage() {
//        checkInitialized()
//
//        // Get initial route from config
//        val initialRoute = configProvider.getInitialRoute()
//        CreatePage(initialRoute)
//    }
//
//    /**
//     * Registers a custom widget builder.
//     *
//     * @param type The widget type identifier (e.g., "custom/myWidget")
//     * @param builder The builder function that creates the widget
//     */
//    fun registerWidget(type: String, builder: VirtualWidgetBuilder) {
//        checkInitialized()
//        widgetRegistry.registerWidget(type, builder)
//        Logger.log("Registered custom widget: $type")
//    }
//
//    /**
//     * Sets an environment variable that can be accessed in expressions.
//     *
//     * @param key The variable name
//     * @param value The variable value
//     */
//    fun setEnvironmentVariable(key: String, value: Any?) {
//        environmentVariables[key] = value
//    }
//
//    /**
//     * Sets multiple environment variables at once.
//     *
//     * @param variables Map of variable names to values
//     */
//    fun setEnvironmentVariables(variables: Map<String, Any?>) {
//        environmentVariables.putAll(variables)
//    }
//
//    /**
//     * Gets an environment variable value.
//     *
//     * @param key The variable name
//     * @return The variable value or null if not found
//     */
//    fun getEnvironmentVariable(key: String): Any? {
//        return environmentVariables[key]
//    }
//
//    /**
//     * Gets all environment variables.
//     *
//     * @return Map of all environment variables
//     */
//    fun getAllEnvironmentVariables(): Map<String, Any?> {
//        return environmentVariables.toMap()
//    }
//
//    /** Clears all environment variables. */
//    fun clearEnvironmentVariables() {
//        environmentVariables.clear()
//    }
//
//    /** Destroys the factory and cleans up resources. */
//    fun destroy() {
//        environmentVariables.clear()
//        isInitialized = false
//        Logger.log("DUIFactory destroyed")
//    }
//
//    private fun checkInitialized() {
//        if (!isInitialized) {
//            throw IllegalStateException(
//                    "DUIFactory is not initialized. Call DUIFactory.getInstance().initialize() first."
//            )
//        }
//    }
//
//    companion object {
//        @Volatile private var instance: DUIFactory? = null
//
//        /**
//         * Gets the singleton instance of DUIFactory.
//         *
//         * @return The DUIFactory singleton instance
//         */
//        fun getInstance(): DUIFactory {
//            return instance
//                    ?: synchronized(this) { instance ?: DUIFactory().also { instance = it } }
//        }
//    }
//}
//
///** UI Resources container holding colors, fonts, icons, and images. */
//data class UIResources(
//        val colors: Map<String, Any> = emptyMap(),
//        val darkColors: Map<String, Any> = emptyMap(),
//        val fonts: Map<String, Any> = emptyMap(),
//        val customIcons: Map<String, Any>? = null,
//        val customImages: Map<String, Any>? = null
//)
