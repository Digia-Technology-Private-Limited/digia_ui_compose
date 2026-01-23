package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.registerAllChildern
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.NumUtil

/** Opacity widget - makes its child partially transparent. */
class VWOpacity(
        refName: String? = null,
        commonProps: CommonProps? = null,
        private val opacityProps: OpacityProps,
        parent: VirtualNode? = null,
        slots: ((VirtualCompositeNode<OpacityProps>) -> Map<String, List<VirtualNode>>?)? = null,
        parentProps: Props? = null
) :
        VirtualCompositeNode<OpacityProps>(
                props = opacityProps,
                commonProps = commonProps,
                parentProps = parentProps,
                parent = parent,
                refName = refName,
                _slots = slots
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        val opacity = (opacityProps.opacity ?: 1.0).toFloat()
        val alwaysIncludeSemantics = opacityProps.alwaysIncludeSemantics ?: false

        // In Compose, semantics are generally preserved with alpha.
        // We use Modifier.alpha to apply transparency.

        var modifier = Modifier.buildModifier(payload)
        modifier = modifier.alpha(opacity)

        Box(modifier = modifier) { child?.ToWidget(payload) }
    }
}

// ============== Props ==============

data class OpacityProps(val opacity: Double? = null, val alwaysIncludeSemantics: Boolean? = null) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): OpacityProps {
            return OpacityProps(
                    opacity = NumUtil.toDouble(json["opacity"]),
                    alwaysIncludeSemantics = json["alwaysIncludeSemantics"] as? Boolean
            )
        }
    }
}

// ============== Builder ==============

fun opacityBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWOpacity(
            refName = data.refName,
            commonProps = data.commonProps,
            opacityProps = OpacityProps.fromJson(data.props.value),
            slots = { self -> registerAllChildern(data.childGroups, self, registry) },
            parent = parent,
            parentProps = data.parentProps
    )
}
