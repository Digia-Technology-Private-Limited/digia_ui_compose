//package com.digia.digiaui.config
//
//import android.content.Context
//import com.digia.digiaui.config.model.DUIConfig
//import com.digia.digiaui.framework.models.ComponentDefinition
//import com.digia.digiaui.framework.models.JsonLike
//import com.digia.digiaui.framework.models.PageDefinition
//import com.digia.digiaui.network.NetworkClient
//import com.digia.digiaui.utils.AssetBundleOperations
//import com.digia.digiaui.utils.AssetBundleOperationsImpl
//import com.digia.digiaui.utils.DownloadOperations
//import com.digia.digiaui.utils.DownloadOperationsImpl
//import com.digia.digiaui.utils.FileOperations
//import com.digia.digiaui.utils.FileOperationsImpl
//
///**
// * Default implementation of ConfigProvider that wraps a DUIConfig.
// *
// * This provides access to pages, components, and other configuration data,
// * and handles network operations through the NetworkClient.
// */
//class DUIConfigProvider(
//    private val config: DUIConfig,
//    override val networkClient: NetworkClient,
//    private val context: Context
//) : ConfigProvider {
//
//    // Initialize operations
//    override val bundleOps: AssetBundleOperations by lazy {
//        AssetBundleOperationsImpl(context.assets)
//    }
//
//    override val fileOps: FileOperations by lazy {
//        FileOperationsImpl(context)
//    }
//
//    override val downloadOps: DownloadOperations by lazy {
//        DownloadOperationsImpl(context)
//    }
//
//    override suspend fun getAppConfigFromNetwork(path: String): DUIConfig {
//        // Use network client to fetch config
//        // For now, return existing config - this would be implemented fully
//        return config
//    }
//
//    override suspend fun initFunctions(
//        remotePath: String?,
//        localPath: String?,
//        version: Int?
//    ) {
//        // Placeholder for JavaScript function initialization
//        // This would load and initialize custom functions if needed
//    }
//
//    override fun addBranchName(branchName: String?) {
//        // Add branch name to network client headers if needed
//    }
//
//    override fun addVersionHeader(version: Int) {
//        // Add version header to network client
//    }
//
//    /**
//     * Gets a page definition by ID
//     */
//    fun getPageDefinition(pageId: String): PageDefinition? {
//        val pageJson = config.pages[pageId] as? JsonLike ?: return null
//        return PageDefinition.fromJson(pageJson)
//    }
//
//    /**
//     * Gets a component definition by ID
//     */
//    fun getComponentDefinition(componentId: String): ComponentDefinition? {
//        val componentJson = config.components?.get(componentId) as? JsonLike ?: return null
//        return ComponentDefinition.fromJson(componentJson)
//    }
//
//    /**
//     * Gets the initial route from app settings
//     */
//    fun getInitialRoute(): String {
//        // First try the initialRoute from appSettings
//        val initialRoute = config.appSettings.initialRoute
//        if (initialRoute.isNotEmpty()) {
//            return initialRoute
//        }
//
//        // Fallback to first page if initialRoute is empty
//        return config.pages.keys.firstOrNull()
//            ?: throw IllegalStateException("No pages defined in configuration")
//    }
//}
