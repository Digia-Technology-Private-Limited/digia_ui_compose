package com.digia.digiaui.sdk.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.sdk.DigiaSDK
import com.digia.digiaui.sdk.api.DigiaHost

class DefaultDigiaHost {
    @Composable

     fun CreatePage(
            startPageId: String?,
            pageArgs: Map<String, Any?>?,
            overrideIcons: Map<String, ImageVector>?,
            overrideImages: Map<String, ImageBitmap>?,
            overrideTextStyles: Map<String, TextStyle>?,
            overrideColors: Map<String, Color>?,
            overrideDarkColors: Map<String, Color>?,
    ) {
     DigiaWrapper {
         DUIFactory.getInstance()
             .CreateNavHost(
                 startPageId = startPageId,
                 pageArgs = pageArgs,
                 overrideIcons = overrideIcons,
                 overrideImages = overrideImages,
                 overrideTextStyles = overrideTextStyles,
                 overrideColors = overrideColors,
                 overrideDarkColors = overrideDarkColors,
             )
     }
    }

    @Composable

     fun CreateComponent(
            componentName: String,
            args: Map<String, Any?>?,
            overrideIcons: Map<String, ImageVector>?,
            overrideImages: Map<String, ImageBitmap>?,
            overrideTextStyles: Map<String, TextStyle>?,
            overrideColors: Map<String, Color>?,
            overrideDarkColors: Map<String, Color>?
    ) {
     DigiaWrapper {
         DUIFactory.getInstance()
             .CreateComponent(
                 componentId = componentName,
                 args = args,
                 overrideIcons = overrideIcons,
                 overrideImages = overrideImages,
                 overrideTextStyles = overrideTextStyles,
                 overrideColors = overrideColors,
                 overrideDarkColors = overrideDarkColors,
             )
     }
    }
}

@Composable
fun DefaultDigiaLoader() {
    Text("Loading...")
}

@Composable
fun DefaultDigiaError(error: Throwable) {
    Text(error.message?.let { "Initialization failed: $it" } ?: "Initialization failed")
}

@Composable
fun DigiaWrapper(
        modifier: Modifier = Modifier,
        loadingContent: @Composable () -> Unit = { DefaultDigiaLoader() },
        errorContent: @Composable (Throwable) -> Unit = { DefaultDigiaError(it) },
        successContent: @Composable () -> Unit = {}
) {

    // The SDK manages its own state internally
    val sdkState by
            produceState<Result<Unit>?>(initialValue = null) {
                try {
                    DigiaSDK.initJob.await()
                    value = Result.success(Unit)
                } catch (e: Exception) {
                    value = Result.failure(e)
                }
            }

    Box(modifier = modifier) {
        when (val result = sdkState) {
            null -> loadingContent() // SDK shows the loader
            else -> {
                result.fold(onSuccess = { successContent() }, onFailure = { errorContent(it) })
            }
        }
    }
}
