# Consumer ProGuard rules for digia-ui

# Keep Material Icons extension properties (accessed via reflection in MaterialIcons.kt)
-keep class androidx.compose.material.icons.filled.** { *; }
-keep class androidx.compose.material.icons.outlined.** { *; }
-keep class androidx.compose.material.icons.rounded.** { *; }
-keep class androidx.compose.material.icons.sharp.** { *; }

# QuickJS native wrapper
-keep class com.whl.quickjs.** { *; }
-dontwarn com.whl.quickjs.**

# Markwon
-dontwarn io.noties.markwon.**

# Lottie
-dontwarn com.airbnb.lottie.**

# YouTube Player
-dontwarn com.pierfrancescosoffritti.**

# Scratchify
-dontwarn io.github.gsrathoreniks.scratchify.**

# Keep Digia public API
-keep class com.digia.digiaui.core.** { public *; }
-keep class com.digia.digiaui.init.** { public *; }
