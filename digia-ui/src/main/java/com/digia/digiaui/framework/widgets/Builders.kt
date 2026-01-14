package com.digia.digiaui.framework.widgets

import com.digia.digiaui.framework.DefaultVirtualWidgetRegistry

/** Register all built-in widgets with the registry */
fun DefaultVirtualWidgetRegistry.registerBuiltInWidgets() {
    // Register Text widget
    register("digia/text", ::textBuilder)

    // Register layout widgets
    register("digia/column", ::columnBuilder)
    register("digia/row", ::rowBuilder)
    
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
}
