package com.digia.digiaui.framework.widgets

import com.digia.digiaui.framework.DefaultVirtualWidgetRegistry
import com.digia.digiaui.framework.widgets.icon.iconBuilder

/** Register all built-in widgets with the registry */
fun DefaultVirtualWidgetRegistry.registerBuiltInWidgets() {
    // Register Text widget
    register("digia/text", ::textBuilder)

    // Register layout widgets
    register("digia/column", ::columnBuilder)
    register("digia/row", ::rowBuilder)

    // Register list widget
    register("digia/listView", ::listViewBuilder)

    // Registering the Container widget
    register("digia/container", ::containerBuilder)

    // Register Button Widget widget
    register("digia/button", ::buttonBuilder) 

    register("digia/lottie", ::lottieBuilder)

    // Register Icon widget
    register("digia/icon", ::iconBuilder)

    // Register Wrap widget
    register("digia/wrap", ::wrapBuilder)
    register("digia/videoPlayer", ::videoPlayerBuilder)

    // Register Scaffold widget (commented out for now)
    // register("digia/scaffold", ::scaffoldBuilder)
}
