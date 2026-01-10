package com.digia.digiaui.framework.widgets.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.getIcon
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike

/**
 * Virtual Icon widget that renders Material Icons
 */
class VWIcon(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: IconProps
) : VirtualLeafNode<IconProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Resolve icon from iconData (pack + key)
        val imageVector: ImageVector? = payload.getIcon(props.iconData)

        if (imageVector == null) {
            // No valid icon to render
            return
        }

        // Optional size support if IconProps exposes it (e.g. ExprOr<Double> size)
        val iconSize: Double? = payload.evalExpr(props.size)
        val iconColor: Color? = payload.evalColor(props.color)

        var modifier: Modifier = Modifier.buildModifier(payload)
        if (iconSize != null) {
            modifier = modifier.size(iconSize.dp)
        }

        Icon(
            imageVector = imageVector,
            contentDescription = "icon",
            modifier = modifier,
            tint = iconColor ?: Color.Unspecified
        )
    }
}

/**
 * Builder function to construct a VWIcon from node data
 */
@Suppress("UNUSED_PARAMETER")
fun iconBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    return VWIcon(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.parentProps,
        props = IconProps.fromJson(data.props.value)
    )
}
