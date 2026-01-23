package com.digia.digiaui.framework.base

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.utils.applyCommonProps

abstract class VirtualLeafNode<T>(
    val props: T,
    val commonProps: CommonProps?,
    parent: VirtualNode?,
    refName: String?,
    parentProps: Props? = null
) : VirtualNode(refName, parent, parentProps) {

    @Composable
    override fun ToWidget(payload: RenderPayload) {

        val extendedPayload =
            refName?.let { payload.withExtendedHierarchy(it) } ?: payload

        val isVisible =
            commonProps?.visibility?.let { extendedPayload.evalExpr(it) } ?: true

        if (!isVisible) return


        Render(extendedPayload)

    }


    @Composable
    override fun Modifier.buildModifier(payload: RenderPayload): Modifier {
        return this.applyCommonProps(payload, commonProps)
    }
}






fun String.toComposeAlignment(): Alignment =
    when (this) {
        "center" -> Alignment.Center
        "topLeft" -> Alignment.TopStart
        "topRight" -> Alignment.TopEnd
        "bottomLeft" -> Alignment.BottomStart
        "bottomRight" -> Alignment.BottomEnd
        "centerLeft" -> Alignment.CenterStart
        "centerRight" -> Alignment.CenterEnd
        "topCenter" -> Alignment.TopCenter
        "bottomCenter" -> Alignment.BottomCenter
        else -> Alignment.Center
    }
