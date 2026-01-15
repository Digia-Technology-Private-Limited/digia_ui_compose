package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Stack widget properties
 * 
 * Maps to Flutter Stack widget with properties from schema:
 * - childAlignment: alignment for non-positioned children (topStart, center, etc.)
 * - fit: how stack sizes itself relative to children (expand, loose, passthrough)
 */
data class StackProps(
    val childAlignment: String? = null,
    val fit: String? = null
) {
    companion object {
        fun fromJson(json: JsonLike): StackProps {
            return StackProps(
                childAlignment = json["childAlignment"] as? String,
                fit = json["fit"] as? String
            )
        }
    }
}

/**
 * Positioning data extracted from parentProps
 * 
 * Mirrors Flutter's Positioned widget:
 * - left: distance from left edge of Stack (in dp)
 * - top: distance from top edge of Stack (in dp)
 * - right: distance from right edge of Stack (in dp)
 * - bottom: distance from bottom edge of Stack (in dp)
 */
data class PositionData(
    val left: Double? = null,
    val top: Double? = null,
    val right: Double? = null,
    val bottom: Double? = null
) {
    fun hasAnyPositioning(): Boolean {
        return left != null || top != null || right != null || bottom != null
    }
    
    /**
     * Convert dp value to pixels using the provided density
     */
    fun leftPx(density: Density): Int? = left?.let { with(density) { it.dp.roundToPx() } }
    fun topPx(density: Density): Int? = top?.let { with(density) { it.dp.roundToPx() } }
    fun rightPx(density: Density): Int? = right?.let { with(density) { it.dp.roundToPx() } }
    fun bottomPx(density: Density): Int? = bottom?.let { with(density) { it.dp.roundToPx() } }
    
    companion object {
        /**
         * Parses position string in format "left,top,right,bottom"
         * where "-" or empty represents null
         */
        fun fromString(positionStr: String): PositionData {
            val parts = positionStr.split(',').map { it.trim() }
            
            fun parse(value: String): Double? {
                return if (value.isEmpty() || value == "-") null 
                else value.toDoubleOrNull()
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
                bottom = (json["bottom"] as? Number)?.toDouble()
            )
        }
    }
}

/**
 * Virtual Stack widget
 *
 * Renders children in a stack layout similar to Flutter's Stack widget.
 * Children are laid out on top of each other with optional positioning.
 *
 * Flutter's Stack/Positioned behavior:
 * 1. Non-positioned children are aligned using the `alignment` property and sized loosely
 * 2. Positioned children are placed absolutely from specified edges
 * 3. If both left and right are specified, child width is constrained
 * 4. If both top and bottom are specified, child height is constrained
 * 5. Stack sizes itself based on `fit`:
 *    - loose: wraps the largest non-positioned child
 *    - expand: fills parent
 *    - passthrough: passes constraints through
 */
class VWStack(
    refName: String? = null,
    commonProps: CommonProps? = null,
    props: StackProps,
    parent: VirtualNode? = null,
    slots: ((VirtualCompositeNode<StackProps>) -> Map<String, List<VirtualNode>>?)? = null,
    parentProps: Props? = null
) : VirtualCompositeNode<StackProps>(
    props = props,
    commonProps = commonProps,
    parentProps = parentProps,
    parent = parent,
    refName = refName,
    _slots = slots
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Return empty if no children
        if (children.isEmpty()) {
            Empty()
            return
        }

        val alignment = toAlignment(props.childAlignment)
        val stackFit = props.fit ?: "loose"
        
        // Build base modifier with common props
        val baseModifier = Modifier.buildModifier(payload)
        
        // Apply fit modifier based on fit type
        val boxModifier = when (stackFit.lowercase()) {
            "expand" -> baseModifier.fillMaxSize()
            "passthrough" -> baseModifier.wrapContentSize(unbounded = true)
            else -> baseModifier // loose (default) - wrap content
        }

        // Collect position data for each child
        val childPositions = children.map { extractPosition(it) }
        
        // Get density for dp to pixel conversion
        val density = LocalDensity.current

        // Debug log position data
        childPositions.forEachIndexed { index, pos ->
            android.util.Log.d("VWStack", "Child[$index] position: left=${pos?.left}, top=${pos?.top}, right=${pos?.right}, bottom=${pos?.bottom}")
        }

        // Use custom layout to properly position children like Flutter's Stack
        StackLayout(
            modifier = boxModifier,
            alignment = alignment,
            stackFit = stackFit,
            childPositions = childPositions,
            density = density
        ) {
            children.forEach { child ->
                Box {
                    child.ToWidget(payload)
                }
            }
        }
    }

    /**
     * Extracts position data from child's parentProps
     */
    private fun extractPosition(child: VirtualNode): PositionData? {
        val parentProps = child.parentProps
        val positionValue = parentProps?.value?.get("position")
        
        // Debug logging
        android.util.Log.d("VWStack", "extractPosition - Child: ${child.refName}, parentProps: ${parentProps?.value}, position: $positionValue")
        
        if (parentProps == null || positionValue == null) return null
        
        val result = when (positionValue) {
            is String -> PositionData.fromString(positionValue)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                PositionData.fromJson(positionValue as JsonLike)
            }
            else -> null
        }
        
        android.util.Log.d("VWStack", "extractPosition - Parsed: left=${result?.left}, top=${result?.top}, right=${result?.right}, bottom=${result?.bottom}")
        return result
    }

    /**
     * Converts alignment string to Compose Alignment
     */
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
            else -> Alignment.TopStart // Default to topStart as per schema
        }
    }
}

/**
 * Custom Layout composable that mimics Flutter's Stack positioning behavior.
 * 
 * This properly handles:
 * 1. Non-positioned children: aligned using alignment property
 * 2. Positioned children: absolute positioning from edges
 * 3. Width/height constraints when both left+right or top+bottom are specified
 */
@Composable
private fun StackLayout(
    modifier: Modifier,
    alignment: Alignment,
    stackFit: String,
    childPositions: List<PositionData?>,
    density: Density,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        // For loose fit, we need to first measure non-positioned children to determine stack size
        // For expand fit, we use parent constraints
        
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        
        // Pre-measure non-positioned children to determine stack size for loose fit
        val nonPositionedMeasurements = mutableMapOf<Int, Placeable>()
        var maxNonPositionedWidth = 0
        var maxNonPositionedHeight = 0
        
        if (stackFit.lowercase() != "expand") {
            measurables.forEachIndexed { index, measurable ->
                val position = childPositions.getOrNull(index)
                val isPositioned = position?.hasAnyPositioning() == true
                
                if (!isPositioned) {
                    val placeable = measurable.measure(looseConstraints)
                    nonPositionedMeasurements[index] = placeable
                    maxNonPositionedWidth = max(maxNonPositionedWidth, placeable.width)
                    maxNonPositionedHeight = max(maxNonPositionedHeight, placeable.height)
                }
            }
        }
        
        // Determine Stack size based on fit
        val stackWidth = when (stackFit.lowercase()) {
            "expand" -> constraints.maxWidth
            else -> if (constraints.hasBoundedWidth) {
                max(maxNonPositionedWidth, constraints.minWidth).coerceAtMost(constraints.maxWidth)
            } else {
                max(maxNonPositionedWidth, constraints.minWidth)
            }
        }
        
        val stackHeight = when (stackFit.lowercase()) {
            "expand" -> constraints.maxHeight
            else -> if (constraints.hasBoundedHeight) {
                max(maxNonPositionedHeight, constraints.minHeight).coerceAtMost(constraints.maxHeight)
            } else {
                max(maxNonPositionedHeight, constraints.minHeight)
            }
        }
        
        android.util.Log.d("VWStack", "StackLayout - stackWidth=$stackWidth, stackHeight=$stackHeight, fit=$stackFit")
        
        // Measure all children with final stack size known
        val placeables = measurables.mapIndexed { index, measurable ->
            val position = childPositions.getOrNull(index)
            val isPositioned = position?.hasAnyPositioning() == true
            
            if (!isPositioned) {
                // Use pre-measured value if available (loose fit), otherwise measure now (expand fit)
                nonPositionedMeasurements[index] ?: measurable.measure(
                    if (stackFit.lowercase() == "expand") constraints else looseConstraints
                )
            } else {
                // Measure positioned child with appropriate constraints
                measurePositionedChild(measurable, position, stackWidth, stackHeight, constraints, density)
            }
        }
        
        layout(stackWidth, stackHeight) {
            placeables.forEachIndexed { index, placeable ->
                val position = childPositions.getOrNull(index)
                
                if (position?.hasAnyPositioning() == true) {
                    // Positioned child: calculate position from edges using density for dp->px conversion
                    val (x, y) = calculatePositionedOffset(position, placeable, stackWidth, stackHeight, density)
                    android.util.Log.d("VWStack", "Placing child[$index] at x=$x, y=$y (placeable: ${placeable.width}x${placeable.height})")
                    placeable.placeRelative(x, y)
                } else {
                    // Non-positioned child: use alignment
                    val alignmentOffset = alignment.align(
                        size = androidx.compose.ui.unit.IntSize(placeable.width, placeable.height),
                        space = androidx.compose.ui.unit.IntSize(stackWidth, stackHeight),
                        layoutDirection = layoutDirection
                    )
                    android.util.Log.d("VWStack", "Aligning child[$index] at x=${alignmentOffset.x}, y=${alignmentOffset.y}")
                    placeable.placeRelative(alignmentOffset.x, alignmentOffset.y)
                }
            }
        }
    }
}

/**
 * Measures a positioned child with appropriate constraints based on position data.
 * 
 * Flutter's Positioned behavior:
 * - If both left and right are specified: width = stackWidth - left - right
 * - If both top and bottom are specified: height = stackHeight - top - bottom
 * - Otherwise: use natural size
 */
private fun measurePositionedChild(
    measurable: Measurable,
    position: PositionData?,
    stackWidth: Int,
    stackHeight: Int,
    parentConstraints: Constraints,
    density: Density
): Placeable {
    // Convert dp values to pixels using density
    val leftPx = position?.leftPx(density) ?: 0
    val topPx = position?.topPx(density) ?: 0
    val rightPx = position?.rightPx(density) ?: 0
    val bottomPx = position?.bottomPx(density) ?: 0
    
    val hasLeft = position?.left != null
    val hasRight = position?.right != null
    val hasTop = position?.top != null
    val hasBottom = position?.bottom != null
    
    // Calculate constrained width if both left and right are specified
    val constrainedWidth = if (hasLeft && hasRight) {
        (stackWidth - leftPx - rightPx).coerceAtLeast(0)
    } else {
        null
    }
    
    // Calculate constrained height if both top and bottom are specified
    val constrainedHeight = if (hasTop && hasBottom) {
        (stackHeight - topPx - bottomPx).coerceAtLeast(0)
    } else {
        null
    }
    
    val childConstraints = Constraints(
        minWidth = constrainedWidth ?: 0,
        maxWidth = constrainedWidth ?: parentConstraints.maxWidth,
        minHeight = constrainedHeight ?: 0,
        maxHeight = constrainedHeight ?: parentConstraints.maxHeight
    )
    
    android.util.Log.d("VWStack", "measurePositionedChild - hasLeft=$hasLeft, hasRight=$hasRight, hasTop=$hasTop, hasBottom=$hasBottom, constrainedWidth=$constrainedWidth, constrainedHeight=$constrainedHeight")
    
    return measurable.measure(childConstraints)
}

/**
 * Calculates the x, y offset for a positioned child.
 * 
 * Flutter's Positioned positioning:
 * - left: x = left
 * - right (no left): x = stackWidth - right - childWidth
 * - top: y = top
 * - bottom (no top): y = stackHeight - bottom - childHeight
 * - both left and right: x = left (width is already constrained)
 * - both top and bottom: y = top (height is already constrained)
 */
private fun calculatePositionedOffset(
    position: PositionData,
    placeable: Placeable,
    stackWidth: Int,
    stackHeight: Int,
    density: Density
): Pair<Int, Int> {
    // Convert dp values to pixels using density
    val leftPx = position.leftPx(density)
    val topPx = position.topPx(density)
    val rightPx = position.rightPx(density)
    val bottomPx = position.bottomPx(density)
    
    // Calculate X position
    val x = when {
        leftPx != null -> leftPx
        rightPx != null -> stackWidth - rightPx - placeable.width
        else -> 0
    }
    
    // Calculate Y position
    val y = when {
        topPx != null -> topPx
        bottomPx != null -> stackHeight - bottomPx - placeable.height
        else -> 0
    }
    
    android.util.Log.d("VWStack", "calculatePositionedOffset - leftPx=$leftPx, topPx=$topPx, rightPx=$rightPx, bottomPx=$bottomPx -> x=$x, y=$y")
    
    return Pair(x, y)
}

/** Builder function for Stack widget */
fun stackBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {

    return VWStack(
        refName = data.refName,
        commonProps = data.commonProps,
        props = StackProps.fromJson(data.props.value),
        slots = {
                self ->
            registerAllChildern(data.childGroups, self, registry)
        },
        parent = parent,
        parentProps = data.parentProps
    )
}