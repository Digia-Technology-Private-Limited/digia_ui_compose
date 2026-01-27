package com.digia.digiaui.framework.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a "natural" shadow that is directed downwards relative to the elevation.
 *
 * @param elevation The elevation value which determines the vertical offset and blur radius.
 * @param shape The shape of the shadow (should match the component's shape).
 * @param color The color of the shadow.
 * @param alpha The opacity of the shadow (default 0.2f).
 */
fun Modifier.naturalShadow(
        elevation: Dp,
        shape: Shape,
        color: Color = Color.Black,
        alpha: Float = 0.2f
): Modifier {
    // If no elevation, return unmodified
    if (elevation <= 0.dp) return this

    return this.drawBehind {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        val transparentColor = color.copy(alpha = 0f).toArgb()

        val elevationPx = elevation.toPx()
        val dy = elevationPx // Move shadow down by elevation amount
        val dx = 0f
        val radius = elevationPx * 2 // Blur is 2x elevation for softer look

        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(radius, dx, dy, shadowColor)

            val outline = shape.createOutline(size, layoutDirection, this)
            canvas.drawOutline(outline, paint)
        }
    }
}
