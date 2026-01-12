package com.digia.digiaui.framework.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.textStyle
import com.digia.digiaui.framework.utils.JsonLike

/** Text widget properties */
data class TextProps(
        val text: ExprOr<String>?,
        val textStyle: JsonLike? = null,
        val maxLines: ExprOr<Int>? = null,
        val alignment: ExprOr<String>? = null,
        val overflow: ExprOr<String>? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): TextProps {
            return TextProps(
                    text = ExprOr.fromValue(json["text"]),
                    textStyle = json["textStyle"] as? JsonLike,
                    maxLines = ExprOr.fromValue(json["maxLines"]),
                    alignment = ExprOr.fromValue(json["alignment"]),
                    overflow = ExprOr.fromValue(json["overflow"])
            )
        }
    }
}

/** Virtual Text widget */
class VWText(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: TextProps
) : VirtualLeafNode<TextProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate expressions
        val text = payload.evalObserve(props.text) ?: ""
        val style = payload.textStyle(props.textStyle)
        val maxLines = payload.evalExpr(props.maxLines)
        val alignmentStr = payload.evalExpr(props.alignment)
        val overflowStr = payload.evalExpr(props.overflow)

        // Convert string values to Compose types
        val textAlign =
                when (alignmentStr) {
                    "center" -> TextAlign.Center
                    "left" -> TextAlign.Left
                    "right" -> TextAlign.Right
                    "start" -> TextAlign.Start
                    "end" -> TextAlign.End
                    "justify" -> TextAlign.Justify
                    else -> null
                }

        val textOverflow =
                when (overflowStr) {
                    "clip" -> TextOverflow.Clip
                    "ellipsis" -> TextOverflow.Ellipsis
                    "visible" -> TextOverflow.Visible
                    else -> TextOverflow.Clip
                }

        // Render Material3 Text
        Text(
                text = text.toString(),
                style = style ?: androidx.compose.ui.text.TextStyle.Default,
                maxLines = maxLines ?: Int.MAX_VALUE,
                textAlign = textAlign,
                overflow = textOverflow,
            modifier = Modifier.buildModifier(payload)
        )
    }
}

/** Builder function for Text widget */
fun textBuilder(data: VWNodeData, parent: VirtualNode?,registry: VirtualWidgetRegistry): VirtualNode {
    return VWText(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.props,
        props = TextProps.fromJson(data.props.value)
    )
}
