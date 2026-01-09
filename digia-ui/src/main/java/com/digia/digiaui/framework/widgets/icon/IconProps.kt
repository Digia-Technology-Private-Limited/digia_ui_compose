package com.digia.digiaui.framework.widgets.icon

import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike

/** Icon widget properties */
data class IconProps(
        val iconData: JsonLike? = null,
        val color: ExprOr<String>? = null,
        val size: ExprOr<Double>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): IconProps {
            return IconProps(
                    iconData = json["iconData"] as? JsonLike,
                    color = ExprOr.fromValue(json["color"]),
                    size = ExprOr.fromValue(json["size"])
            )
        }
    }
}
