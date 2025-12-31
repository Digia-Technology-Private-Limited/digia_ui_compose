//package com.digia.digiaui.framework.base
//
//import androidx.compose.runtime.Composable
//import com.digia.digiaui.framework.RenderPayload
//import com.digia.digiaui.framework.models.CommonProps
//
///** Base class for all virtual widgets Mirrors Flutter VirtualWidget */
//abstract class VirtualWidget {
//    abstract val refName: String?
//    abstract val commonProps: CommonProps?
//
//    /** Render this virtual widget to a Compose widget */
//    @Composable abstract fun render(payload: RenderPayload)
//
//    /** Empty widget (like SizedBox.shrink() in Flutter) */
//    @Composable
//    fun empty() {
//        // Compose doesn't render anything
//    }
//
//    /** Convert to widget - main entry point for rendering */
//    @Composable
//    fun toWidget(payload: RenderPayload) {
//        // Check visibility
//        val visible = commonProps?.visible?.let { payload.evalExpr(it) } ?: true
//        if (!visible) {
//            empty()
//            return
//        }
//
//        // Render the widget
//        render(payload)
//    }
//}
//
///** Virtual leaf widget - for widgets with no children (Text, Image, etc.) */
//abstract class VirtualLeafWidget : VirtualWidget()
//
///** Virtual builder widget - for widgets that build on demand */
//class VirtualBuilderWidget(
//        override val refName: String?,
//        override val commonProps: CommonProps?,
//        val builder: @Composable (RenderPayload) -> Unit
//) : VirtualWidget() {
//    @Composable
//    override fun render(payload: RenderPayload) {
//        builder(payload)
//    }
//}
