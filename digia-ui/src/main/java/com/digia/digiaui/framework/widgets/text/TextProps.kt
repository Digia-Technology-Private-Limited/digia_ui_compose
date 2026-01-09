package com.digia.digiaui.framework.widgets.text

import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike

/**
 * Text style properties
 */
data class TextStyleProps(
    val textColor: ExprOr<String>? = null,
    val fontSize: ExprOr<Double>? = null,
    val fontWeight: ExprOr<String>? = null,
    val fontFamily: ExprOr<String>? = null,
    val letterSpacing: ExprOr<Double>? = null,
    val lineHeight: ExprOr<Double>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): TextStyleProps {
            return TextStyleProps(
                textColor = ExprOr.fromValue(json["textColor"]),
                fontSize = ExprOr.fromValue(json["fontSize"]),
                fontWeight = ExprOr.fromValue(json["fontWeight"]),
                fontFamily = ExprOr.fromValue(json["fontFamily"]),
                letterSpacing = ExprOr.fromValue(json["letterSpacing"]),
                lineHeight = ExprOr.fromValue(json["lineHeight"])
            )
        }
    }
}

/**
 * Text widget properties
 */
data class TextProps(
    val text: ExprOr<String>? = null,
    val textStyle: TextStyleProps? = null,
    val maxLines: ExprOr<Int>? = null,
    val alignment: ExprOr<String>? = null,
    val overflow: ExprOr<String>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): TextProps {
            return TextProps(
                text = ExprOr.fromValue(json["text"]),
                textStyle = (json["textStyle"] as? JsonLike)?.let { TextStyleProps.fromJson(it) },
                maxLines = ExprOr.fromValue(json["maxLines"]),
                alignment = ExprOr.fromValue(json["alignment"]),
                overflow = ExprOr.fromValue(json["overflow"])
            )
        }
    }
}
