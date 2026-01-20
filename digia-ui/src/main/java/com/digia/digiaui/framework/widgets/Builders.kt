package com.digia.digiaui.framework.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.digia.digiaui.framework.DefaultVirtualWidgetRegistry
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData

/** Register all built-in widgets with the registry */
fun DefaultVirtualWidgetRegistry.registerBuiltInWidgets() {
    // Register Text widget
    register("digia/text", ::textBuilder)

    // Register layout widgets
    register("digia/column", ::columnBuilder)
    register("digia/row", ::rowBuilder)
    //    register("digia/stack", ::stackBuilder)

    // Register list widget
    register("digia/listView", ::listViewBuilder)

    register("digia/streamBuilder", ::streamBuilderBuilder)

    register("digia/conditionalBuilder", ::conditionalBuilder)
    register("digia/conditionalItem", ::conditionalItemBuilder)

    // Register Scaffold widget (commented out for now)
    register("fw/scaffold", ::scaffoldBuilder)
    // Register AppBar widget
    register("digia/appBar", ::appBarBuilder)
    register("fw/appBar", ::appBarBuilder)
    register("digia/circularProgressBar", ::circularProgressBarBuilder)
    register("digia/futureBuilder", ::futureBuilder)
    register("digia/lottie", ::lottieBuilder)
    register("digia/linearProgressBar", ::linearProgressBarBuilder)
    register("digia/textFormField", ::textFormFieldBuilder)
    register("digia/videoPlayer", ::videoPlayerBuilder)
    register("digia/button", ::buttonBuilder)
    register("digia/image", ::imageBuilder)
    register("digia/container", ::containerBuilder)
    register("digia/carousel", ::carouselBuilder)
    register("digia/wrap", ::wrapBuilder)

    register("digia/stack", ::stackBuilder)
    register("digia/styledHorizontalDivider", ::styledHorizontalDividerBuilder)

    register("fw/sizedBox", ::sizedBoxBuilder)
    register("fw/sized_box", ::sizedBoxBuilder)
    register("digia/gridView", ::gridViewBuilder)
    register("gridView", ::gridViewBuilder) // Alias
    register("digia/masonryGridView", ::gridViewBuilder)
    register("digia/calendar", ::dummyBuilder)
}

// Update dummyBuilder signature and call
fun dummyBuilder(
        nodeData: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWDummy(
            type = nodeData.type, // Pass type
            refName = nodeData.refName,
            commonProps = nodeData.commonProps,
            parent = parent,
            parentProps = nodeData.parentProps,
            props = nodeData.props
    )
}

class VWDummy(
        val type: String, // Add type property
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: Props
) :
        VirtualLeafNode<Props>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        val message = payload.evalExpr(ExprOr.fromValue(props.get("message"))) ?: "No Message"

        Text(
                text = "DUMMY [${type}]\nMsg: $message\nKeys: ${props.value.keys}",
                modifier = Modifier.buildModifier(payload)
        )
    }
}
