package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData

/** Virtual CircularProgressBar widget */
class VWCircularProgressBar(
        props: Props,
        commonProps: CommonProps?,
        parentProps: Props?,
        parent: VirtualNode?,
        refName: String?
) :
        VirtualLeafNode<Props>(
                props = props,
                commonProps = commonProps,
                parentProps = parentProps,
                parent = parent,
                refName = refName
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {

        val progressValue = payload.eval<Double>(props.get("progressValue"))

        val type = props.getString("type") ?: "indeterminate"

        val size = props.getDouble("size")?.dp ?: 40.dp

        val thickness = props.getDouble("thickness")?.dp ?: 4.dp

        val indicatorColor = payload.evalColor(props.get("indicatorColor")) ?: Color.Blue

        val bgColor = payload.evalColor(props.get("bgColor")) ?: Color.Transparent

        val animate = props.getBool("animation") ?: false
        val animateFromLastPercent = props.getBool("animateFromLastPercent") ?: false

        when (type) {
            "indeterminate" -> {
                CircularProgressIndicator(
                        modifier = Modifier.size(size),
                        color = indicatorColor,
                        strokeWidth = thickness,
                        trackColor = bgColor
                )
            }
            else -> {
                // Determine target progress
                val targetProgress = ((progressValue ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat()

                val progress =
                        androidx.compose.runtime.remember {
                            androidx.compose.animation.core.Animatable(
                                    if (animate && !animateFromLastPercent) 0f else targetProgress
                            )
                        }

                androidx.compose.runtime.LaunchedEffect(
                        targetProgress,
                        animate,
                        animateFromLastPercent
                ) {
                    if (animate) {
                        if (!animateFromLastPercent) {
                            // If not animating from last percent, snap to 0 first (except on
                            // initial load if we want that behavior,
                            // but usually "No" means "Start from 0 every time value changes" or
                            // "Start from 0 on init")
                            // Matching Flutter behavior: if animateFromLastPercent is false, it
                            // starts from 0.
                            // However, we need to be careful not to snap to 0 if we are already
                            // there or if it's a stable state update.
                            // A simple interpretation: Snap to 0f, then animate to target.
                            progress.snapTo(0f)
                        }
                        progress.animateTo(
                                targetValue = targetProgress,
                                animationSpec =
                                        androidx.compose.animation.core.tween(
                                                durationMillis = 500
                                        ) // Default duration to match typical UI feel
                        )
                    } else {
                        progress.snapTo(targetProgress)
                    }
                }

                CircularProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.size(size),
                        color = indicatorColor,
                        strokeWidth = thickness,
                        trackColor = bgColor,
                        strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

fun circularProgressBarBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWCircularProgressBar(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.parentProps ?: Props.empty(),
            props = data.props,
    )
}
