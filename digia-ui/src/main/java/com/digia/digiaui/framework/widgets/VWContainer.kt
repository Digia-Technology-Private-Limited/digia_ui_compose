package com.digia.digiaui.framework.widgets

import LocalUIResources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.actions.LocalActionExecutor
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.state.LocalStateContextProvider
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.NumUtil
import com.digia.digiaui.framework.utils.ToUtils
import com.digia.digiaui.framework.utils.applyIf
import com.digia.digiaui.framework.utils.toDp

/**
 * Container widget - uses commonProps for most styling, adds gradient/elevation/alignment
 */
class VWContainer(
    refName: String? = null,
    commonProps: CommonProps? = null,
    private val containerProps: ContainerProps,
    parent: VirtualNode? = null,
    slots: Map<String, List<VirtualNode>>? = null,
    parentProps: Props? = null
) : VirtualCompositeNode<ContainerProps>(
    props = containerProps,
    commonProps = commonProps,
    parentProps = parentProps,
    parent = parent,
    refName = refName,
    slots = slots
) {

    @Composable
    override fun Render(payload: RenderPayload) {
        val context = LocalContext.current.applicationContext
        val actionExecutor = LocalActionExecutor.current
        val stateContext = LocalStateContextProvider.current
        val resources = LocalUIResources.current

        val isCircle = containerProps.shape == "circle"
        val borderRadius = ToUtils.borderRadius(containerProps.borderRadius)
        val shape: Shape = if (isCircle) CircleShape else borderRadius

        val gradient = containerProps.gradient?.toBrush(payload)
        val elevation = (containerProps.elevation ?: 0.0).dp
        val alignment = containerProps.childAlignment.toAlignment()
        val bgColor = payload.evalColor(containerProps.color)

        // Build modifier
        var modifier = Modifier.buildModifier(payload)

        // Apply margin (outer spacing)
        modifier = modifier.padding(ToUtils.edgeInsets(containerProps.margin))

        // Apply shadow/elevation first (before clipping)
        if (elevation > 0.dp) {
            modifier = modifier.shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
        }

        // Apply custom shadows
        containerProps.shadow?.let { shadows ->
            modifier = modifier.drawBehind {
                shadows.forEach { shadowProps ->
                    val shadowColor = Color.Black
                    val offsetX = (shadowProps.offsetX ?: 0.0).dp.toPx()
                    val offsetY = (shadowProps.offsetY ?: 0.0).dp.toPx()

                    drawRect(
                        color = shadowColor.copy(alpha = 0.3f),
                        topLeft = Offset(offsetX, offsetY),
                        size = size
                    )
                }
            }
        }

        // Apply sizing constraints
        modifier = modifier
            .applyIf(containerProps.width != null) { size(width = containerProps.width.toDp()!!, height = Dp.Unspecified) }
            .applyIf(containerProps.height != null) {
                if (containerProps.width != null) size(width = containerProps.width.toDp()!!, height = containerProps.height.toDp()!!)
                else size(width = Dp.Unspecified, height = containerProps.height.toDp()!!)
            }
            .widthIn(min = containerProps.minWidth.toDp() ?: 0.dp, max = containerProps.maxWidth.toDp() ?: Dp.Infinity)
            .heightIn(min = containerProps.minHeight.toDp() ?: 0.dp, max = containerProps.maxHeight.toDp() ?: Dp.Infinity)

        // Apply background (gradient or color)
        modifier = when {
            gradient != null -> modifier.background(brush = gradient, shape = shape)
            bgColor != null -> modifier.background(color = bgColor, shape = shape)
            else -> modifier
        }

        // Apply decoration image
        containerProps.decorationImage?.let { decorationImage ->
            val imageSource = decorationImage.source
            if (!imageSource.isNullOrEmpty() && imageSource.contains("http")) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageSource)
                        .crossfade(true)
                        .build()
                )
                val contentScale = decorationImage.fit.toContentScale()
                val imageAlignment = decorationImage.alignment.toAlignment()
                val alpha = (decorationImage.opacity ?: 1.0).toFloat()

                modifier = modifier.paint(
                    painter = painter,
                    contentScale = contentScale,
                    alignment = imageAlignment,
                    alpha = alpha
                )
            }
        }

        // Apply border
        val border = containerProps.border
        val borderWidth = (border?.borderWidth ?: 0.0).dp
        val borderColor = payload.evalColor(border?.borderColor) ?: Color.Transparent
        if (borderWidth > 0.dp) {
            modifier = modifier.border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
        }

        // Clip content to shape
        modifier = modifier.clip(shape)

        // Apply padding (inner spacing)
        modifier = modifier.padding(ToUtils.edgeInsets(containerProps.padding))

        // Apply onClick gesture
        commonProps?.onClick?.let { actionFlow ->
            if (actionFlow.actions.isNotEmpty()) {
                modifier = modifier.clickable {
                    payload.executeAction(
                        context = context,
                        actionFlow = actionFlow,
                        stateContext = stateContext,
                        resourceProvider = resources,
                        actionExecutor = actionExecutor
                    )
                }
            }
        }

        // Render the container
        if (elevation > 0.dp && (bgColor != null || gradient != null)) {
            // Use Surface for Material elevation effect
            Surface(
                modifier = modifier,
                shape = shape,
                color = bgColor ?: Color.Transparent,
                shadowElevation = elevation
            ) {
                Box(contentAlignment = alignment) {
                    child?.ToWidget(payload)
                }
            }
        } else {
            // Use Box for simple container
            Box(modifier = modifier, contentAlignment = alignment) {
                child?.ToWidget(payload)
            }
        }
    }
}

// ============== Props ==============

data class ContainerProps(
    val shape: String? = null,
    val elevation: Double? = null,
    val borderRadius: Any? = null,
    val childAlignment: String? = null,
    val gradient: GradientProps? = null,
    val shadow: List<ShadowProps>? = null,
    val decorationImage: DecorationImageProps? = null,
    val width: String? = null,
    val height: String? = null,
    val minWidth: String? = null,
    val maxWidth: String? = null,
    val minHeight: String? = null,
    val maxHeight: String? = null,
    val padding: Any? = null,
    val margin: Any? = null,
    val color: Any? = null,
    val border: BorderProps? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): ContainerProps {
            return ContainerProps(
                width = json["width"] as? String,
                height = json["height"] as? String,
                minWidth = json["minWidth"] as? String,
                maxWidth = json["maxWidth"] as? String,
                minHeight = json["minHeight"] as? String,
                maxHeight = json["maxHeight"] as? String,
                padding = json["padding"],
                margin = json["margin"],
                color = json["color"],
                childAlignment = json["childAlignment"] as? String,
                shape = json["shape"] as? String,
                elevation = NumUtil.toDouble(json["elevation"]),
                borderRadius = json["borderRadius"],
                border = (json["border"] as? JsonLike)?.let { BorderProps.fromJson(it) },
                gradient = (json["gradiant"] as? JsonLike)?.let { GradientProps.fromJson(it) },
                shadow = (json["shadow"] as? List<*>)?.mapNotNull { item ->
                    (item as? JsonLike)?.let { ShadowProps.fromJson(it) }
                },
                decorationImage = (json["decorationImage"] as? JsonLike)?.let {
                    DecorationImageProps.fromJson(it)
                }
            )
        }
    }
}

data class BorderProps(
    val borderWidth: Double? = null,
    val borderColor: Any? = null,
    val borderGradient: GradientProps? = null,
    val borderPattern: String? = null,
    val strokeCap: String? = null,
    val dashPattern: List<Double>? = null,
    val strokeAlign: String? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): BorderProps {
            val borderType = json["borderType"] as? JsonLike
            return BorderProps(
                borderWidth = NumUtil.toDouble(json["borderWidth"]),
                borderColor = json["borderColor"],
                borderGradient = (json["borderGradiant"] as? JsonLike)?.let { GradientProps.fromJson(it) },
                borderPattern = borderType?.get("borderPattern") as? String,
                strokeCap = borderType?.get("strokeCap") as? String,
                dashPattern = (borderType?.get("dashPattern") as? List<*>)?.mapNotNull { 
                    NumUtil.toDouble(it) 
                },
                strokeAlign = json["strokeAlign"] as? String
            )
        }
    }
}

data class GradientProps(
    val type: String? = null,
    val colorList: List<ColorStop>? = null,
    val begin: String? = null,
    val end: String? = null,
    val center: String? = null,
    val radius: Double? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): GradientProps {
            val colorListRaw = json["colorList"] as? List<*>
            val colorList = colorListRaw?.mapNotNull { item ->
                (item as? JsonLike)?.let { ColorStop.fromJson(it) }
            }
            
            return GradientProps(
                type = json["type"] as? String,
                colorList = colorList,
                begin = json["begin"] as? String,
                end = json["end"] as? String,
                center = json["center"] as? String,
                radius = NumUtil.toDouble(json["radius"])
            )
        }
    }

    @Composable
    fun toBrush(payload: RenderPayload): Brush? {
        val colorStops = colorList ?: return null
        if (colorStops.isEmpty()) return null

        val colors = colorStops.mapNotNull { payload.evalColor(it.color) }
        if (colors.isEmpty()) return null

        val stops = colorStops.mapNotNull { it.stop?.toFloat() }
        val hasValidStops = stops.size == colors.size

        return when (type) {
            "linear" -> {
                if (hasValidStops) {
                    Brush.linearGradient(
                        colorStops = stops.zip(colors).toTypedArray(),
                        start = begin.toGradientOffset(isEnd = false),
                        end = end.toGradientOffset(isEnd = true)
                    )
                } else {
                    Brush.linearGradient(
                        colors = colors,
                        start = begin.toGradientOffset(isEnd = false),
                        end = end.toGradientOffset(isEnd = true)
                    )
                }
            }
            "angular" -> {
                // "angular" in schema = RadialGradient in Flutter
                val centerOffset = center.toGradientOffset()
                val radiusValue = ((radius ?: 0.5) * 500f).toFloat() // Approximate
                if (hasValidStops) {
                    Brush.radialGradient(
                        colorStops = stops.zip(colors).toTypedArray(),
                        center = centerOffset,
                        radius = radiusValue
                    )
                } else {
                    Brush.radialGradient(
                        colors = colors,
                        center = centerOffset,
                        radius = radiusValue
                    )
                }
            }
            else -> null
        }
    }
}

data class ColorStop(
    val color: Any? = null,
    val stop: Double? = null
) {
    companion object {
        fun fromJson(json: JsonLike) = ColorStop(
            color = json["color"],
            stop = NumUtil.toDouble(json["stop"])
        )
    }
}

data class ShadowProps(
    val color: Any? = null,
    val blur: Double? = null,
    val spreadRadius: Double? = null,
    val offsetX: Double? = null,
    val offsetY: Double? = null,
    val blurStyle: String? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ShadowProps {
            return ShadowProps(
                color = json["color"],
                blur = NumUtil.toDouble(json["blur"]),
                spreadRadius = NumUtil.toDouble(json["spreadRadius"]),
                offsetX = NumUtil.toDouble((json["offset"] as? JsonLike)?.get("x")),
                offsetY = NumUtil.toDouble((json["offset"] as? JsonLike)?.get("y")),
                blurStyle = json["blurStyle"] as? String
            )
        }
    }
}

data class DecorationImageProps(
    val source: String? = null,
    val sourceType: String? = null,
    val imageType: String? = null,
    val fit: String? = null,
    val alignment: String? = null,
    val opacity: Double? = null
) {
    companion object {
        fun fromJson(json: JsonLike): DecorationImageProps {
            return DecorationImageProps(
                source = json["path"] as? String ?: json["source"] as? String,
                sourceType = json["sourceType"] as? String,
                imageType = json["imageType"] as? String,
                fit = json["fit"] as? String,
                alignment = json["alignment"] as? String,
                opacity = NumUtil.toDouble(json["opacity"])?.let { it / 100.0 }
            )
        }
    }
}

// ============== Extensions ==============

private fun String?.toAlignment(): Alignment = when (this) {
    "topLeft", "topStart" -> Alignment.TopStart
    "topCenter" -> Alignment.TopCenter
    "topRight", "topEnd" -> Alignment.TopEnd
    "centerLeft", "centerStart" -> Alignment.CenterStart
    "center" -> Alignment.Center
    "centerRight", "centerEnd" -> Alignment.CenterEnd
    "bottomLeft", "bottomStart" -> Alignment.BottomStart
    "bottomCenter" -> Alignment.BottomCenter
    "bottomRight", "bottomEnd" -> Alignment.BottomEnd
    else -> Alignment.TopStart
}

private fun String?.toContentScale(): ContentScale = when (this) {
    "cover" -> ContentScale.Crop
    "fill" -> ContentScale.FillBounds
    "fitWidth" -> ContentScale.FillWidth
    "fitHeight" -> ContentScale.FillHeight
    "none" -> ContentScale.None
    "scaleDown" -> ContentScale.Inside
    "contain" -> ContentScale.Fit
    else -> ContentScale.Crop
}

private fun String?.toGradientOffset(isEnd: Boolean = false): Offset = when (this) {
    "topLeft" -> Offset.Zero
    "topCenter" -> Offset(Float.POSITIVE_INFINITY, 0f)  // Will be clamped to width/2, 0
    "topRight" -> Offset(Float.POSITIVE_INFINITY, 0f)
    "centerLeft" -> Offset(0f, Float.POSITIVE_INFINITY)
    "center" -> Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY / 2)
    "centerRight" -> Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY / 2)
    "bottomLeft" -> Offset(0f, Float.POSITIVE_INFINITY)
    "bottomCenter" -> Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY)
    "bottomRight" -> Offset.Infinite
    else -> if (isEnd) Offset.Infinite else Offset.Zero
}

// ============== Builder ==============

fun containerBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData = data.childGroups?.mapValues { (_, childrenData) ->
        childrenData.map { childData ->
            registry.createWidget(childData, parent)
        }
    }

    return VWContainer(
        refName = data.refName,
        commonProps = data.commonProps,
        containerProps = ContainerProps.fromJson(data.props.value),
        slots = childrenData,
        parent = parent,
        parentProps = data.parentProps
    )
}