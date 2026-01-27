package com.digia.digiaui.framework.components.dui_icons.packs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector

object MaterialIcons {
    private val iconMap: Map<String, ImageVector> =
            mapOf(
                    "account_circle" to Icons.Filled.AccountCircle,
                    "ac_unit" to Icons.Filled.AcUnit,
                    "access_alarm" to Icons.Filled.AccessAlarm,
                    "accessibility" to Icons.Filled.Accessibility,
                    "add" to Icons.Filled.Add
            )

    fun getMaterialIcon(name: String): ImageVector? {
        return iconMap[name]
    }
}
