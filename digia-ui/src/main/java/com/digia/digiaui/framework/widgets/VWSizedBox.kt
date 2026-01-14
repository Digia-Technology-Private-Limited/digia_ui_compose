package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.NumUtil


class SizedBoxProps(
    val height: Double?,
    val width: Double?
) {

    companion object {
        fun fromJson(json: JsonLike): SizedBoxProps {
            return SizedBoxProps(
                height = NumUtil.toDouble(json["height"]),
                width = NumUtil.toDouble(json["width"])
            )
        }
    }
}


class VWSizedBox(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: SizedBoxProps
) : VirtualLeafNode<SizedBoxProps>(
    props = props,
    commonProps = commonProps,
    parent = parent,
    refName = refName,
    parentProps = parentProps
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // 1. Start with an initial modifier (ideally passed in, or empty)
        var modifier: Modifier = Modifier

        // 2. Reassign the result of the modifier chain
        props.width?.let { width ->
            modifier = modifier.then(Modifier.width(width.toDp()))
        }
        props.height?.let { height ->
            modifier = modifier.then(Modifier.height(height.toDp()))
        }

        Box(
            modifier = modifier,
            content = {}
        )
    }

    // Helper extension function to convert to dp
    private fun Number.toDp(): Dp = this.toFloat().dp
}


fun sizedBoxBuilder(data: VWNodeData, parent: VirtualNode?, registry: VirtualWidgetRegistry): VirtualNode {
    return VWSizedBox(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.parentProps,
        props = SizedBoxProps.fromJson(data.props.value)
    )
}