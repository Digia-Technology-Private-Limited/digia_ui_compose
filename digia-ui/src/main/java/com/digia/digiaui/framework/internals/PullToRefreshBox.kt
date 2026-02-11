package com.digia.digiaui.framework.internals

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * `PullToRefreshBox` wrapper used by Digia widgets.
 *
 * Delegates to Material3's built-in pull-to-refresh (available since M3 1.3.0).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.Unspecified,
    indicatorBackgroundColor: Color = Color.Transparent,
    indicatorTopPadding: Dp = 0.dp,
    refreshingOffset: Dp = 80.dp,
    refreshThreshold: Dp = 80.dp,
    strokeWidth: Dp = 2.dp,
    enabled: Boolean = true,
    triggerMode: String? = null,
    content: @Composable () -> Unit,
) {
    val effectiveEnabled = enabled && triggerMode != "disabled"
    val state = rememberPullToRefreshState()

    Box(
        modifier = modifier.pullToRefresh(
            isRefreshing = refreshing,
            state = state,
            enabled = effectiveEnabled,
            onRefresh = onRefresh,
        )
    ) {
        content()

        PullToRefreshDefaults.Indicator(
            state = state,
            isRefreshing = refreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            color = if (indicatorColor == Color.Unspecified) Color.Blue else indicatorColor,
        )
    }
}
