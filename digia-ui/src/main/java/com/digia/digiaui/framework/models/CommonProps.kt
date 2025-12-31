package com.digia.digiaui.framework.models

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/** Common properties shared by all widgets (padding, margin, visibility, etc.) */
data class CommonProps(
        val padding: PaddingValues? = null,
        val margin: PaddingValues? = null,
        val visible: ExprOr<Boolean>? = null,
        val enabled: ExprOr<Boolean>? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): CommonProps {
            if (json == null) return CommonProps()

            return CommonProps(
                    padding = parsePadding(json["padding"]),
                    margin = parsePadding(json["margin"]),
                    visible = ExprOr.fromValue(json["visible"]),
                    enabled = ExprOr.fromValue(json["enabled"])
            )
        }

        private fun parsePadding(value: Any?): PaddingValues? {
            return when (value) {
                is Number -> PaddingValues(value.toDouble().dp)
                is Map<*, *> -> {
                    val map = value as Map<String, Any>
                    PaddingValues(
                            start = (map["left"] as? Number)?.toDouble()?.dp ?: 0.dp,
                            top = (map["top"] as? Number)?.toDouble()?.dp ?: 0.dp,
                            end = (map["right"] as? Number)?.toDouble()?.dp ?: 0.dp,
                            bottom = (map["bottom"] as? Number)?.toDouble()?.dp ?: 0.dp
                    )
                }
                else -> null
            }
        }
    }
}
