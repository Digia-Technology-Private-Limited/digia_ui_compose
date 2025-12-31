//package com.digia.digiaui.framework
//
//import androidx.compose.runtime.Composable
//import com.digia.digiaui.framework.base.VirtualBuilderWidget
//import com.digia.digiaui.framework.base.VirtualWidget
//import com.digia.digiaui.framework.models.VWComponentData
//import com.digia.digiaui.framework.models.VWData
//import com.digia.digiaui.framework.models.VWNodeData
//import com.digia.digiaui.framework.models.VWStateData
//
///** Type alias for widget builder functions */
//typealias VirtualWidgetBuilder = (VWNodeData, VirtualWidgetRegistry) -> VirtualWidget
//
///** Type alias for component builder functions */
//typealias ComponentBuilder = @Composable (String, Map<String, Any?>?) -> Unit
//
///**
// * Virtual widget registry - manages widget types and builders Mirrors Flutter VirtualWidgetRegistry
// */
//interface VirtualWidgetRegistry {
//    fun createWidget(data: VWData): VirtualWidget
//    fun registerWidget(type: String, builder: VirtualWidgetBuilder)
//}
//
///** Default virtual widget registry implementation */
//class DefaultVirtualWidgetRegistry(private val componentBuilder: ComponentBuilder) :
//        VirtualWidgetRegistry {
//
//    private val builders = mutableMapOf<String, VirtualWidgetBuilder>()
//
//    init {
//        // Register default widgets - we'll add these incrementally
//        // For now, just Text and Scaffold
//    }
//
//    override fun createWidget(data: VWData): VirtualWidget {
//        return when (data) {
//            is VWNodeData -> {
//                val builder =
//                        builders[data.type]
//                                ?: throw IllegalArgumentException(
//                                        "Unknown widget type: ${data.type}"
//                                )
//                builder(data, this)
//            }
//            is VWComponentData -> {
//                // Create a builder widget that renders the component
//                VirtualBuilderWidget(
//                        refName = data.refName,
//                        commonProps = data.commonProps,
//                        builder = { payload ->
//                            val args = data.args?.mapValues { payload.evalExpr(it.value) }
//                            componentBuilder(data.id, args)
//                        }
//                )
//            }
//            is VWStateData -> {
//                // State containers would be implemented with State management
//                // For now, just render children
//                throw NotImplementedError("State containers not yet implemented")
//            }
//        }
//    }
//
//    override fun registerWidget(type: String, builder: VirtualWidgetBuilder) {
//        builders[type] = builder
//    }
//
//    /** Helper to register a widget with this registry */
//    internal fun register(type: String, builder: VirtualWidgetBuilder) {
//        builders[type] = builder
//    }
//}
