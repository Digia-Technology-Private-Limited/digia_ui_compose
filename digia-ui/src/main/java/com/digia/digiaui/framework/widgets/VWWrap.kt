package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike

@OptIn(ExperimentalLayoutApi::class)
class VWWrap(
    refName: String? = null,
    commonProps: CommonProps? = null,
    props: Props,
    parent: VirtualNode? = null,
    slots: Map<String, List<VirtualNode>>? = null,
    parentProps: Props? = null
) : VirtualCompositeNode<Props>(
    props = props,
    commonProps = commonProps,
    parentProps = parentProps,
    parent = parent,
    refName = refName,
    slots = slots
) {

    private val shouldRepeatChild: Boolean
        get() = props.value["dataSource"] != null

    @Composable
    override fun Render(payload: RenderPayload) {
        // children are in the "children" slot
        val wrapChildren = slots?.get("children")

        if (wrapChildren.isNullOrEmpty()) {
            Empty()
            return
        }

        // Data-source driven children vs direct children
        val composableChildren: @Composable () -> Unit =
            if (shouldRepeatChild) {
                {
                    val childToRepeat = wrapChildren.first()
                    val items: List<Any> =
                        payload.eval<List<Any>>(props.value["dataSource"]) ?: emptyList()

                    items.forEachIndexed { index, item ->
                        val scopedPayload = payload.copyWithChainedContext(
                            createExprContext(item, index)
                        )
                        childToRepeat.ToWidget(scopedPayload)
                    }
                }
            } else {
                {
                    wrapChildren.forEach { child ->
                        child.ToWidget(payload)
                    }
                }
            }

        // Read props
        val spacing = payload.eval<Double>(props.value["spacing"]) ?: 0.0
        val runSpacing = payload.eval<Double>(props.value["runSpacing"]) ?: 0.0
        val wrapAlignmentStr = payload.eval<String>(props.value["wrapAlignment"]) ?: "start"
        val runAlignmentStr = payload.eval<String>(props.value["runAlignment"]) ?: "start"
        val directionStr = payload.eval<String>(props.value["direction"]) ?: "horizontal"

        @Suppress("UNUSED_VARIABLE")
        val wrapCrossAlignmentStr =
            payload.eval<String>(props.value["wrapCrossAlignment"]) ?: "start"
        @Suppress("UNUSED_VARIABLE")
        val verticalDirectionStr =
            payload.eval<String>(props.value["verticalDirection"]) ?: "down"
        @Suppress("UNUSED_VARIABLE")
        val clipBehaviorStr =
            payload.eval<String>(props.value["clipBehavior"]) ?: "none"

        val spacingDp = spacing.dp
        val runSpacingDp = runSpacing.dp

        val horizontalArrangement =
            wrapAlignmentToHorizontalArrangement(wrapAlignmentStr, spacingDp)
        val verticalArrangement =
            runAlignmentToVerticalArrangement(runAlignmentStr, runSpacingDp)

        // Note: verticalDirection / clipBehavior / wrapCrossAlignment are not
        // directly supported by FlowRow/FlowColumn; can be wired later via
        // custom layout or item modifiers.[web:40]

        if (directionStr == "vertical") {
            FlowColumn(
                modifier = Modifier,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement
            ) {
                composableChildren()
            }
        } else {
            FlowRow(
                modifier = Modifier,
                horizontalArrangement = horizontalArrangement,
                verticalArrangement = verticalArrangement
            ) {
                composableChildren()
            }
        }
    }

    private fun createExprContext(item: Any?, index: Int): DefaultScopeContext {
        val wrapObj = mapOf("currentItem" to item, "index" to index)

        val variables = mutableMapOf<String, Any?>().apply {
            putAll(wrapObj)
            refName?.let { name -> put(name, wrapObj) }
        }

        return DefaultScopeContext(variables = variables)
    }
}

/**
 * Map wrapAlignment string -> horizontal Arrangement with spacing.
 */
private fun wrapAlignmentToHorizontalArrangement(
    value: String,
    spacing: Dp
): Arrangement.Horizontal {
    val base = when (value) {
        "center" -> Alignment.CenterHorizontally
        "end", "right" -> Alignment.End
        else -> Alignment.Start
    }

    return if (spacing > 0.dp) {
        Arrangement.spacedBy(spacing, alignment = base)
    } else {
        when (value) {
            "center" -> Arrangement.Center
            "end", "right" -> Arrangement.End
            "spaceBetween" -> Arrangement.SpaceBetween
            "spaceAround" -> Arrangement.SpaceAround
            "spaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
    }
}

/**
 * Map runAlignment string -> vertical Arrangement with runSpacing.
 */
private fun runAlignmentToVerticalArrangement(
    value: String,
    runSpacing: Dp
): Arrangement.Vertical {
    val base = when (value) {
        "center" -> Alignment.CenterVertically
        "end", "bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }

    return if (runSpacing > 0.dp) {
        Arrangement.spacedBy(runSpacing, alignment = base)
    } else {
        when (value) {
            "center" -> Arrangement.Center
            "end", "bottom" -> Arrangement.Bottom
            "spaceBetween" -> Arrangement.SpaceBetween
            "spaceAround" -> Arrangement.SpaceAround
            "spaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Top
        }
    }
}

fun wrapBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData = data.childGroups?.mapValues { (_, childrenData) ->
        childrenData.map { childData -> registry.createWidget(childData, parent) }
    }

    return VWWrap(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.parentProps,
        props = data.props,
        slots = childrenData
    )
}
