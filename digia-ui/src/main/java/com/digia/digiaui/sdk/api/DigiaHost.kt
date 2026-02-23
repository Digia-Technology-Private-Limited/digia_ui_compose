package com.digia.digiaui.sdk.api

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle

/** Only navigation-host related operations. */
interface DigiaHost {
    @Composable
    fun CreatePage(
        startPageId: String? = null,
        pageArgs: Map<String, Any?>? = null,
        overrideIcons: Map<String, ImageVector>? = null,
        overrideImages: Map<String, ImageBitmap>? = null,
        overrideTextStyles: Map<String, TextStyle>? = null,
        overrideColors: Map<String, Color>? = null,
        overrideDarkColors: Map<String, Color>? = null,
    )


    @Composable
    fun CreateComponent(
        componentName: String,
        args: Map<String, Any?>? = null,
        overrideIcons: Map<String, ImageVector>? = null,
        overrideImages: Map<String, ImageBitmap>? = null,
        overrideTextStyles: Map<String, TextStyle>? = null,
        overrideColors: Map<String, Color>? = null,
        overrideDarkColors: Map<String, Color>? = null,
    )

}
