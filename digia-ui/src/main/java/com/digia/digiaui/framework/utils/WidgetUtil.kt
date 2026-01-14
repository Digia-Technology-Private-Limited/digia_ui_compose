package com.digia.digiaui.framework.utils

import LocalUIResources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.actions.LocalActionExecutor
import com.digia.digiaui.framework.actions.base.ActionFlow
import com.digia.digiaui.framework.color
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.CommonStyle
import com.digia.digiaui.framework.state.LocalStateContextProvider

val PaddingValuesZero = PaddingValues(0.dp)


@Composable
fun Modifier.applyCommonProps(
    payload: RenderPayload,
    commonProps: CommonProps?
): Modifier {
    if (commonProps == null) return this

    // Capture action executor and context at Composable scope level
    val actionExecutor = LocalActionExecutor.current
    // Use applicationContext to prevent memory leaks - it lives for app lifetime
    val context = LocalContext.current.applicationContext
    val re = LocalUIResources.current

    val stateContext= LocalStateContextProvider.current
    val style = commonProps.style

    var modifier = this


    if (style != null) {
        val padding = ToUtils.edgeInsets(style.padding)
        val bgColor = style.bgColor
            ?.evaluate<String>(payload.scopeContext)
            ?.let { payload.color(it) }

        val borderRadius = ToUtils.borderRadius(style.borderRadius)
        val border = style.border

        val borderColor =
            payload.color(border?.get("borderColor") as? String ?: "")

        val borderWidth =
            (border?.get("borderWidth") as? Number)?.toFloat()

        val borderPattern =
            (border?.get("borderType") as? JsonLike)
                ?.get("borderPattern") as? String

        modifier = modifier.applyIf(padding!= PaddingValuesZero) {
                padding(padding)
            }
            .applyIf(bgColor != null) {
                background(bgColor!!, shape = borderRadius)
            }
            .applyIf(
                borderPattern == "solid" &&
                        borderWidth != null &&
                        borderWidth > 0f
            ) {
                border(
                    width = borderWidth?.dp?:0.dp,
                    color = borderColor ?: Color.Black,
                    shape = borderRadius
                )
            }
            .applyIf(borderRadius!=RoundedCornerShape(0.dp)) {
                clip(borderRadius)
            }
            .applySizing(style)
    }

    // Gesture
    val actionFlow = commonProps.onClick
    if (actionFlow != null && actionFlow.actions.isNotEmpty()) {
        modifier = modifier.clickable {
            payload.executeAction(
                context = context,
                actionFlow = actionFlow,
                stateContext= stateContext,
                resourceProvider = re,
                actionExecutor = actionExecutor
            )
        }
    }
    val padding = ToUtils.edgeInsets(style?.margin)
    modifier = modifier.applyIf(padding!= PaddingValuesZero) {
        padding(padding)
    }



    return modifier
}




//private fun Modifier.applySizing(style: CommonStyle): Modifier {
//    val isWidthIntrinsic = style.width.equals("intrinsic", true)
//    val isHeightIntrinsic = style.height.equals("intrinsic", true)
//
//    var m = this
//
//    if (isWidthIntrinsic) {
//        m = m.width(IntrinsicSize.Min)
//    }
//
//    if (isHeightIntrinsic) {
//        m = m.height(IntrinsicSize.Min)
//    }
//
//    val width = if (!isWidthIntrinsic) style.width.toDp() else null
//    val height = if (!isHeightIntrinsic) style.height.toDp() else null
//
//    if (width != null || height != null) {
//        m = m.then(
//            Modifier.size(
//                width = width ?: Dp.Unspecified,
//                height = height ?: Dp.Unspecified
//            )
//        )
//    }
//
//    return m
//}




fun Any?.toDp(): Dp? = when (this) {
    null -> null
    is Dp -> this
    is Number -> this.toFloat().dp
    is String -> {
        val v = trim().lowercase()
        when {
            v == "auto" -> null
            v == "intrinsic" -> null
            v.endsWith("dp") -> v.removeSuffix("dp").toFloatOrNull()?.dp
            v.endsWith("px") -> v.removeSuffix("px").toFloatOrNull()?.dp // engine rule
            else -> v.toFloatOrNull()?.dp
        }
    }
    else -> null
}

fun Any?.toPercentFraction(): Float? {
    if (this !is String) return null
    val v = trim().lowercase()
    if (v.endsWith("%")) {
        return v.removeSuffix("%").toFloatOrNull()?.let { it / 100f }
    }
    return null
}




inline fun Modifier.applyIf(
    condition: Boolean,
    block: Modifier.() -> Modifier
): Modifier {
    return if (condition) {
        this.block()
    } else {
        this
    }
}


private fun Modifier.applySizing(style: CommonStyle): Modifier {
    var m = this

    // Width logic
    val wPercent = style.width.toPercentFraction()
    if (wPercent != null) {
        m = m.fillMaxWidth(wPercent)
    } else {
        style.width.toDp()?.let { m = m.width(it) }
    }

    // Height logic
    val hPercent = style.height.toPercentFraction()
    if (hPercent != null) {
        m = m.fillMaxHeight(hPercent)
    } else {
        style.height.toDp()?.let { m = m.height(it) }
    }

    if (style.width.equals("intrinsic", true)) m = m.width(IntrinsicSize.Min)
    if (style.height.equals("intrinsic", true)) m = m.height(IntrinsicSize.Min)

    return m
}