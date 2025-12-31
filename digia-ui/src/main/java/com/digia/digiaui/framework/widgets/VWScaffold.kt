//package com.digia.digiaui.framework.widgets
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import com.digia.digiaui.framework.RenderPayload
//import com.digia.digiaui.framework.VirtualWidgetRegistry
//import com.digia.digiaui.framework.base.VirtualWidget
//import com.digia.digiaui.framework.models.CommonProps
//import com.digia.digiaui.framework.models.ExprOr
//import com.digia.digiaui.framework.models.JsonLike
//import com.digia.digiaui.framework.models.VWNodeData
//
///** Scaffold widget properties */
//data class ScaffoldProps(
//        val title: ExprOr<String>? = null,
//        val showAppBar: ExprOr<Boolean>? = null
//) {
//    companion object {
//        fun fromJson(json: JsonLike): ScaffoldProps {
//            return ScaffoldProps(
//                    title = ExprOr.fromValue(json["title"]),
//                    showAppBar = ExprOr.fromValue(json["showAppBar"])
//            )
//        }
//    }
//}
//
///** Virtual Scaffold widget */
//class VWScaffold(
//        override val refName: String?,
//        override val commonProps: CommonProps?,
//        val props: ScaffoldProps,
//        val body: VirtualWidget?
//) : VirtualWidget() {
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    override fun render(payload: RenderPayload) {
//        // Evaluate expressions
//        val title = payload.evalExpr(props.title) ?: ""
//        val showAppBar = payload.evalExpr(props.showAppBar) ?: true
//
//        // Render Material3 Scaffold
//        Scaffold(
//                topBar = {
//                    if (showAppBar) {
//                        TopAppBar(title = { Text(title.toString()) })
//                    }
//                }
//        ) { paddingValues ->
//            // Create Box with padding to render body
//            Box(modifier = Modifier.padding(paddingValues)) {
//                // Render body
//                val bodyPayload = payload.copy(context = LocalContext.current)
//                body?.toWidget(bodyPayload)
//            }
//        }
//    }
//}
//
///** Builder function for Scaffold widget */
//fun scaffoldBuilder(data: VWNodeData, registry: VirtualWidgetRegistry): VirtualWidget {
//    // Get body child from childGroups
//    val bodyData = data.childGroups?.get("body")?.firstOrNull()
//    val bodyWidget = bodyData?.let { registry.createWidget(it) }
//
//    return VWScaffold(
//            refName = data.refName,
//            commonProps = data.commonProps,
//            props = ScaffoldProps.fromJson(data.props),
//            body = bodyWidget
//    )
//}
