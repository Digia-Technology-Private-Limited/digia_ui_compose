package com.digia.digiaui.framework.components.dui_icons.packs

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Reflection-based Material Icons resolver.
 *
 * Instead of a static 8000-line when-block that forces every icon class into
 * the DEX, icons are looked up by name at runtime via reflection. This allows
 * R8 to strip unused icon classes when the consuming app enables minification.
 *
 * Naming convention (matches Flutter SDUI payloads):
 *   "icon_name"           -> Icons.Filled.IconName
 *   "icon_name_outlined"  -> Icons.Outlined.IconName
 *   "icon_name_rounded"   -> Icons.Rounded.IconName
 *   "icon_name_sharp"     -> Icons.Sharp.IconName
 */
object MaterialIcons {

    // Caches both hits (ImageVector) and misses (null) to avoid repeated reflection.
    private val iconCache = HashMap<String, ImageVector?>()

    fun getMaterialIcon(name: String): ImageVector? {
        if (iconCache.containsKey(name)) return iconCache[name]
        val icon = resolveViaReflection(name)
        iconCache[name] = icon
        return icon
    }

    private fun resolveViaReflection(name: String): ImageVector? {
        val (baseName, pkg, receiver) = parseIconName(name) ?: return null
        val pascal = snakeToPascal(baseName)
        val className = "androidx.compose.material.icons.${pkg}.${pascal}Kt"
        return try {
            val clazz = Class.forName(className)
            // Extension property compiles to a static getter: get<PascalName>(Icons.<Style>)
            val getter = clazz.declaredMethods.firstOrNull { it.name == "get$pascal" }
                ?: return null
            getter.invoke(null, receiver) as? ImageVector
        } catch (_: Exception) {
            null
        }
    }

    private data class IconMeta(val baseName: String, val pkg: String, val receiver: Any)

    private fun parseIconName(name: String): IconMeta? {
        if (name.isBlank()) return null
        return when {
            name.endsWith("_outlined") -> IconMeta(
                name.removeSuffix("_outlined"), "outlined", Icons.Outlined
            )
            name.endsWith("_rounded") -> IconMeta(
                name.removeSuffix("_rounded"), "rounded", Icons.Rounded
            )
            name.endsWith("_sharp") -> IconMeta(
                name.removeSuffix("_sharp"), "sharp", Icons.Sharp
            )
            else -> IconMeta(name, "filled", Icons.Filled)
        }
    }

    private fun snakeToPascal(snake: String): String =
        snake.split("_").joinToString("") { part ->
            part.replaceFirstChar { it.uppercaseChar() }
        }
}
