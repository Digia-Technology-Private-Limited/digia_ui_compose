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
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.widgets.story.storyBuilder
import com.digia.digiaui.framework.widgets.story.storyVideoPlayerBuilder

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
    // Register layout widgets
    register("digia/column", ::columnBuilder)
    register("digia/row", ::rowBuilder)

    register("digia/stack", ::stackBuilder)

    // Register list widget
    register("digia/listView", ::listViewBuilder)

    // Register page view widget
    register("digia/pageView", ::pageViewBuilder)

    // Register refresh indicator widget
    register("digia/refreshIndicator", ::refreshIndicatorBuilder)

    // Register markdown widget
    register("digia/markdown", ::markdownBuilder)

    // Register web/youtube/animations
    register("digia/youtubePlayer", ::youtubePlayerBuilder)
    register("digia/checkBox", ::checkBoxBuilder)
    register("digia/checkbox", ::checkBoxBuilder)
    register("digia/switch", ::switchBuilder)
    register("digia/webView", ::webViewBuilder)
    register("digia/animatedBuilder", ::animatedBuilderBuilder)
    register("digia/animatedSwitcher", ::animatedSwitcherBuilder)

    register("digia/streamBuilder", ::streamBuilderBuilder)

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
        register("digia/opacity", ::opacityBuilder)
        register("digia/carousel", ::carouselBuilder)
        register("digia/wrap", ::wrapBuilder)
        register("digia/opacity", ::opacityBuilder)
        register("opacity", ::opacityBuilder)
        register("fw/opacity", ::opacityBuilder)

        register("digia/webView", ::webViewBuilder)
        register("fw/webView", ::webViewBuilder)

        register("digia/stack", ::stackBuilder)
        register("digia/styledHorizontalDivider", ::styledHorizontalDividerBuilder)
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

    register("digia/styledHorizontalDivider", ::styledHorizontalDividerBuilder)

    register("fw/sizedBox", ::sizedBoxBuilder)
    register("fw/sized_box", ::sizedBoxBuilder)
    register("digia/gridView", ::dummyBuilder)
    register("digia/richText", ::dummyBuilder)
    register("digia/calendar", ::dummyBuilder)

    register("fw/sizedBox",::sizedBoxBuilder)
    register("fw/sized_box",::sizedBoxBuilder)
    register("digia/gridView",::dummyBuilder)
    register("digia/richText",::richTextBuilder)
    register("digia/timer", ::timerBuilder)
    register("fw/timer", ::timerBuilder)
    register("digia/overlay", ::overlayBuilder)
    register("digia/styledHorizontalDivider",::dummyBuilder)
    register("digia/calendar",::dummyBuilder)
}

        register("fw/sizedBox", ::sizedBoxBuilder)
        register("fw/sized_box", ::sizedBoxBuilder)
        register("digia/gridView", ::dummyBuilder)
        register("digia/richText", ::dummyBuilder)
        register("digia/calendar", ::dummyBuilder)
}

    // Story widgets
    register("digia/story", ::storyBuilder)
    register("digia/storyVideoPlayer", ::storyVideoPlayerBuilder)
}

fun dummyBuilder(
        nodeData: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWDummy(
            refName = nodeData.refName,
            commonProps = nodeData.commonProps,
            parent = parent,
            parentProps = nodeData.parentProps,
            props = nodeData.props
    )
}

class VWDummy(
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
        Text(text = "Dummy Widget Rendered", modifier = Modifier.buildModifier(payload))
    }
}
