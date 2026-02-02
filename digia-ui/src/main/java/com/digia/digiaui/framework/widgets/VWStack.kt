package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.registerAllChildern
import com.digia.digiaui.framework.utils.JsonLike

data class StackProps(val childAlignment: String? = null, val fit: String? = null) {
    companion object {
        fun fromJson(json: JsonLike): StackProps {
            return StackProps(
                    childAlignment = json["childAlignment"] as? String,
                    fit = json["fit"] as? String
            )
        }
    }
}

data class PositionData(
        val left: Double? = null,
        val top: Double? = null,
        val right: Double? = null,
        val bottom: Double? = null,
        val width: Double? = null,
        val height: Double? = null
) {
    companion object {
        fun fromString(positionStr: String): PositionData {
            val parts = positionStr.split(',').map { it.trim() }

            fun parse(value: String): Double? {
                return if (value.isEmpty() || value == "-") null else value.toDoubleOrNull()
            }

            return PositionData(
                    left = if (parts.isNotEmpty()) parse(parts[0]) else null,
                    top = if (parts.size > 1) parse(parts[1]) else null,
                    right = if (parts.size > 2) parse(parts[2]) else null,
                    bottom = if (parts.size > 3) parse(parts[3]) else null
            )
        }

        fun fromJson(json: JsonLike): PositionData {
            return PositionData(
                    left = (json["left"] as? Number)?.toDouble(),
                    top = (json["top"] as? Number)?.toDouble(),
                    right = (json["right"] as? Number)?.toDouble(),
                    bottom = (json["bottom"] as? Number)?.toDouble(),
                    width = (json["width"] as? Number)?.toDouble(),
                    height = (json["height"] as? Number)?.toDouble()
            )
        }
    }
}

class VWStack(
        refName: String? = null,
        commonProps: CommonProps? = null,
        props: StackProps,
        parent: VirtualNode? = null,
        slots: ((VirtualCompositeNode<StackProps>) -> Map<String, List<VirtualNode>>?)? = null,
        parentProps: Props? = null
) :
        VirtualCompositeNode<StackProps>(
                props = props,
                commonProps = commonProps,
                parentProps = parentProps,
                parent = parent,
                refName = refName,
                _slots = slots
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        if (children.isEmpty()) {
            Empty()
            return
        }

        val alignment = toAlignment(props.childAlignment)
        val stackFit = props.fit ?: "loose"
        val baseModifier = Modifier.buildModifier(payload)

        Layout(
                content = {
                    children.forEach { child ->
                        val position = extractPosition(child)
                        val layoutId =
                                if (position != null && hasAnyPositioning(position)) position
                                else null

                        val modifier =
                                if (layoutId != null) Modifier.layoutId(layoutId) else Modifier
                        Box(modifier = modifier) { child.ToWidget(payload) }
                    }
                },
                modifier = baseModifier
        ) { measurables, constraints ->
            val (positionedMeasurables, nonPositionedMeasurables) =
                    measurables.partition { it.layoutId is PositionData }

            val nonPositionedConstraints =
                    when (stackFit.lowercase()) {
                        "expand" ->
                                constraints.copy(
                                        minWidth = constraints.maxWidth,
                                        minHeight = constraints.maxHeight
                                )
                        "passthrough" -> constraints
                        else -> constraints.copy(minWidth = 0, minHeight = 0)
                    }

            val nonPositionedPlaceables =
                    nonPositionedMeasurables.map { it.measure(nonPositionedConstraints) }

            var stackWidth = 0
            var stackHeight = 0

            if (stackFit.lowercase() == "expand") {
                stackWidth = constraints.maxWidth
                stackHeight = constraints.maxHeight
            } else {
                if (nonPositionedPlaceables.isNotEmpty()) {
                    stackWidth = nonPositionedPlaceables.maxOf { it.width }
                    stackHeight = nonPositionedPlaceables.maxOf { it.height }

                    stackWidth = stackWidth.coerceAtLeast(constraints.minWidth)
                    stackHeight = stackHeight.coerceAtLeast(constraints.minHeight)
                } else {
                    stackWidth =
                            if (constraints.hasBoundedWidth) constraints.maxWidth
                            else constraints.minWidth
                    stackHeight =
                            if (constraints.hasBoundedHeight) constraints.maxHeight
                            else constraints.minHeight
                }
            }

            val positionedPlaceables =
                    positionedMeasurables.map { measurable ->
                        val pos = measurable.layoutId as PositionData

                        var childMinWidth = 0
                        var childMaxWidth = stackWidth

                        if (pos.width != null) {
                            val w = pos.width.dp.roundToPx()
                            childMinWidth = w
                            childMaxWidth = w
                        } else if (pos.left != null && pos.right != null) {
                            val l = pos.left.dp.roundToPx()
                            val r = pos.right.dp.roundToPx()
                            val w = (stackWidth - l - r).coerceAtLeast(0)
                            childMinWidth = w
                            childMaxWidth = w
                        } else {
                            childMaxWidth =
                                    if (constraints.hasBoundedWidth) stackWidth
                                    else constraints.maxWidth
                            childMinWidth = 0
                        }

                        var childMinHeight = 0
                        var childMaxHeight = stackHeight

                        if (pos.height != null) {
                            val h = pos.height.dp.roundToPx()
                            childMinHeight = h
                            childMaxHeight = h
                        } else if (pos.top != null && pos.bottom != null) {
                            val t = pos.top.dp.roundToPx()
                            val b = pos.bottom.dp.roundToPx()
                            val h = (stackHeight - t - b).coerceAtLeast(0)
                            childMinHeight = h
                            childMaxHeight = h
                        } else {
                            childMaxHeight =
                                    if (constraints.hasBoundedHeight) stackHeight
                                    else constraints.maxHeight
                            childMinHeight = 0
                        }

                        val childConstraints =
                                Constraints(
                                        minWidth = childMinWidth,
                                        maxWidth = childMaxWidth,
                                        minHeight = childMinHeight,
                                        maxHeight = childMaxHeight
                                )

                        val placeable = measurable.measure(childConstraints)
                        Pair(placeable, pos)
                    }

            layout(stackWidth, stackHeight) {
                nonPositionedPlaceables.forEach { placeable ->
                    val x =
                            alignment.align(
                                            IntSize(placeable.width, placeable.height),
                                            IntSize(stackWidth, stackHeight),
                                            LayoutDirection.Ltr
                                    )
                                    .x
                    val y =
                            alignment.align(
                                            IntSize(placeable.width, placeable.height),
                                            IntSize(stackWidth, stackHeight),
                                            LayoutDirection.Ltr
                                    )
                                    .y
                    placeable.place(x, y)
                }

                positionedPlaceables.forEach { (placeable, pos) ->
                    var x = 0
                    var y = 0

                    if (pos.left != null) {
                        x = pos.left.dp.roundToPx()
                    } else if (pos.right != null) {
                        x = stackWidth - placeable.width - pos.right.dp.roundToPx()
                    } else {
                        x =
                                alignment.align(
                                                IntSize(placeable.width, placeable.height),
                                                IntSize(stackWidth, stackHeight),
                                                LayoutDirection.Ltr
                                        )
                                        .x
                    }

                    if (pos.top != null) {
                        y = pos.top.dp.roundToPx()
                    } else if (pos.bottom != null) {
                        y = stackHeight - placeable.height - pos.bottom.dp.roundToPx()
                    } else {
                        y =
                                alignment.align(
                                                IntSize(placeable.width, placeable.height),
                                                IntSize(stackWidth, stackHeight),
                                                LayoutDirection.Ltr
                                        )
                                        .y
                    }

                    placeable.place(x, y)
                }
            }
        }
    }

    private fun extractPosition(child: VirtualNode): PositionData? {
        val parentProps = child.parentProps ?: return null
        val positionValue = parentProps.value["position"] ?: return null

        return when (positionValue) {
            is String -> PositionData.fromString(positionValue)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST") PositionData.fromJson(positionValue as JsonLike)
            }
            else -> null
        }
    }

    private fun hasAnyPositioning(position: PositionData): Boolean {
        return position.left != null ||
                position.top != null ||
                position.right != null ||
                position.bottom != null ||
                position.width != null ||
                position.height != null
    }

    private fun toAlignment(value: String?): Alignment {
        return when (value?.lowercase()) {
            "topleft", "topstart" -> Alignment.TopStart
            "topcenter" -> Alignment.TopCenter
            "topright", "topend" -> Alignment.TopEnd
            "centerleft", "centerstart" -> Alignment.CenterStart
            "center" -> Alignment.Center
            "centerright", "centerend" -> Alignment.CenterEnd
            "bottomleft", "bottomstart" -> Alignment.BottomStart
            "bottomcenter" -> Alignment.BottomCenter
            "bottomright", "bottomend" -> Alignment.BottomEnd
            else -> Alignment.TopStart
        }
    }
}

fun stackBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData =
            data.childGroups?.mapValues { (_, childrenData) ->
                childrenData.map { childData -> registry.createWidget(childData, parent) }
            }
    return VWStack(
            refName = data.refName,
            commonProps = data.commonProps,
            props = StackProps.fromJson(data.props.value),
            slots = { self -> registerAllChildern(data.childGroups, self, registry) },
            parent = parent,
            parentProps = data.parentProps
    )
}
