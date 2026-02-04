package com.digia.digiaui.framework.components.dui_icons.packs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import com.digia.icons.resolveFilledIcon
import com.digia.icons.resolveOutlinedIcon
import com.digia.icons.resolveRoundedIcon
import com.digia.icons.resolveSharpIcon
import androidx.compose.ui.graphics.vector.ImageVector

fun resolveIcon(key: String): ImageVector? {
    val name = key.lowercase()
    return when {
        name.endsWith("_outlined") -> resolveOutlinedIcon(name.removeSuffix("_outlined"))
        name.endsWith("_rounded") -> resolveRoundedIcon(name.removeSuffix("_rounded"))
        name.endsWith("_sharp") -> resolveSharpIcon(name.removeSuffix("_sharp"))
        else -> resolveFilledIcon(name)
    }
}
