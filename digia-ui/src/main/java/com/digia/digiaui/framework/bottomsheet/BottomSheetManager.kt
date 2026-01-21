package com.digia.digiaui.framework.bottomsheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.framework.DefaultVirtualWidgetRegistry
import com.digia.digiaui.framework.UIResources
import com.digia.digiaui.framework.utils.JsonLike
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing a bottom sheet request
 */
data class BottomSheetRequest(
    val componentId: String,
    val args: JsonLike?,
    val backgroundColor: String?,
    val barrierColor: String?,
    val maxHeightRatio: Float,
    val useSafeArea: Boolean,
    val onDismiss: ((Any?) -> Unit)?
)

/**
 * Manager for showing bottom sheets
 * 
 * Manages bottom sheet display state and provides a way to show/hide bottom sheets
 * from action processors.
 */
class BottomSheetManager {
    private val _currentRequest = MutableStateFlow<BottomSheetRequest?>(null)
    val currentRequest: StateFlow<BottomSheetRequest?> = _currentRequest.asStateFlow()

    /**
     * Show a bottom sheet with the specified component
     */
    fun show(
        componentId: String,
        args: JsonLike? = null,
        backgroundColor: String? = null,
        barrierColor: String? = null,
        maxHeightRatio: Float = 1f,
        useSafeArea: Boolean = true,
        onDismiss: ((Any?) -> Unit)? = null
    ) {
        _currentRequest.value = BottomSheetRequest(
            componentId = componentId,
            args = args,
            backgroundColor = backgroundColor,
            barrierColor = barrierColor,
            maxHeightRatio = maxHeightRatio,
            useSafeArea = useSafeArea,
            onDismiss = onDismiss
        )
    }

    /**
     * Dismiss the current bottom sheet
     */
    fun dismiss(result: Any? = null) {
        val request = _currentRequest.value
        _currentRequest.value = null
        request?.onDismiss?.invoke(result)
    }

    /**
     * Clear the current request without triggering onDismiss
     */
    fun clear() {
        _currentRequest.value = null
    }
}

/**
 * Composable that observes bottom sheet state and displays bottom sheets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetHost(
    bottomSheetManager: BottomSheetManager,
    registry: DefaultVirtualWidgetRegistry,
    resources: UIResources
) {
    val currentRequest by bottomSheetManager.currentRequest.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    LaunchedEffect(currentRequest) {
        if (currentRequest != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    currentRequest?.let { request ->
        ModalBottomSheet(
            onDismissRequest = {
                bottomSheetManager.dismiss()
            },
            sheetState = sheetState,
            containerColor = parseColor(request.backgroundColor) ?: MaterialTheme.colorScheme.surface,
            scrimColor = parseColor(request.barrierColor) ?: BottomSheetDefaults.ScrimColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(request.maxHeightRatio)
        ) {


//            if (componentDef != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (request.useSafeArea) {
                                Modifier.windowInsetsPadding(WindowInsets.systemBars)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    DUIFactory.getInstance().CreateComponent(
                        componentId = request.componentId,
                        args = request.args,
                    )
                }
//            } else {
//                Text(
//                    text = "Component not found: ${request.componentId}",
//                    modifier = Modifier.padding(16.dp),
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
        }
    }
}

/**
 * Parse color string to Compose Color
 * Supports hex colors (#RRGGBB, #AARRGGBB) and named colors
 */
private fun parseColor(colorString: String?): Color? {
    if (colorString == null) return null
    
    return try {
        when {
            colorString.startsWith("#") -> {
                val hex = colorString.substring(1)
                when (hex.length) {
                    6 -> Color(android.graphics.Color.parseColor("#FF$hex"))
                    8 -> Color(android.graphics.Color.parseColor("#$hex"))
                    else -> null
                }
            }
            else -> null // Could add named color support here
        }
    } catch (e: Exception) {
        null
    }
}
