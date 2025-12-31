//package com.digia.digiaui.framework.widgets
//
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import com.digia.digiaui.framework.RenderPayload
//import com.digia.digiaui.framework.VirtualWidgetRegistry
//import com.digia.digiaui.framework.base.VirtualLeafWidget
//import com.digia.digiaui.framework.base.VirtualWidget
//import com.digia.digiaui.framework.models.CommonProps
//import com.digia.digiaui.framework.models.ExprOr
//import com.digia.digiaui.framework.models.JsonLike
//import com.digia.digiaui.framework.models.VWNodeData
//
///** Text widget properties */
//data class TextProps(
//        val text: ExprOr<String>?,
//        val textStyle: String? = null,
//        val maxLines: ExprOr<Int>? = null,
//        val alignment: ExprOr<String>? = null,
//        val overflow: ExprOr<String>? = null
//) {
//    companion object {
//        fun fromJson(json: JsonLike): TextProps {
//            return TextProps(
//                    text = ExprOr.fromValue(json["text"]),
//                    textStyle = json["textStyle"] as? String,
//                    maxLines = ExprOr.fromValue(json["maxLines"]),
//                    alignment = ExprOr.fromValue(json["alignment"]),
//                    overflow = ExprOr.fromValue(json["overflow"])
//            )
//        }
//    }
//}
//
///** Virtual Text widget */
//class VWText(
//        override val refName: String?,
//        override val commonProps: CommonProps?,
//        val props: TextProps
//) : VirtualLeafWidget() {
//
//    @Composable
//    override fun render(payload: RenderPayload) {
//        // Evaluate expressions
//        val text = payload.evalExpr(props.text) ?: ""
//        val style = payload.getTextStyle(props.textStyle)
//        val maxLines = payload.evalExpr(props.maxLines)
//        val alignmentStr = payload.evalExpr(props.alignment)
//        val overflowStr = payload.evalExpr(props.overflow)
//
//        // Convert string values to Compose types
//        val textAlign =
//                when (alignmentStr) {
//                    "center" -> TextAlign.Center
//                    "left" -> TextAlign.Left
//                    "right" -> TextAlign.Right
//                    "start" -> TextAlign.Start
//                    "end" -> TextAlign.End
//                    "justify" -> TextAlign.Justify
//                    else -> null
//                }
//
//        val textOverflow =
//                when (overflowStr) {
//                    "clip" -> TextOverflow.Clip
//                    "ellipsis" -> TextOverflow.Ellipsis
//                    "visible" -> TextOverflow.Visible
//                    else -> TextOverflow.Clip
//                }
//
//        // Render Material3 Text
//        Text(
//                text = text.toString(),
//                style = style ?: androidx.compose.ui.text.TextStyle.Default,
//                maxLines = maxLines ?: Int.MAX_VALUE,
//                textAlign = textAlign,
//                overflow = textOverflow
//        )
//    }
//}
//
///** Builder function for Text widget */
//fun textBuilder(data: VWNodeData, registry: VirtualWidgetRegistry): VirtualWidget {
//    return VWText(
//            refName = data.refName,
//            commonProps = data.commonProps,
//            props = TextProps.fromJson(data.props)
//    )
//}
