package com.digia.digiaui.framework.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BoxShadowData(
        val color: Color = Color.Black,
        val blurRadius: Dp = 0.dp,
        val spreadRadius: Dp = 0.dp,
        val offset: Offset = Offset.Zero
)

fun Modifier.naturalShadow(
        elevation: Dp,
        shape: Shape,
        color: Color = Color.Black,
        alpha: Float = 0.2f
): Modifier {
    if (elevation <= 0.dp) return this

    return this.graphicsLayer { clip = false }.drawWithCache {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        val transparentColor = color.copy(alpha = 0f).toArgb()
        val elevationPx = elevation.toPx()
        val dy = elevationPx
        val dx = 0f
        val radius = elevationPx * 2

        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(radius, dx, dy, shadowColor)

        val outline = shape.createOutline(size, layoutDirection, this)

        onDrawBehind { drawIntoCanvas { canvas -> canvas.drawOutline(outline, paint) } }
    }
}

fun Modifier.drawCustomShadow(shape: Shape, shadows: List<BoxShadowData>?): Modifier {
    if (shadows.isNullOrEmpty()) return this

    return this.graphicsLayer { clip = false }.drawWithCache {
        val paints =
                shadows.map { shadow ->
                    val blurRadiusPx = shadow.blurRadius.toPx()
                    val dx = shadow.offset.x
                    val dy = shadow.offset.y
                    val shadowColor = shadow.color.toArgb()
                    val transparentColor = shadow.color.copy(alpha = 0f).toArgb()

                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.color = transparentColor
                    frameworkPaint.setShadowLayer(blurRadiusPx, dx, dy, shadowColor)
                    paint
                }

        val outline = shape.createOutline(size, layoutDirection, this)

        onDrawBehind {
            drawIntoCanvas { canvas ->
                paints.forEach { paint -> canvas.drawOutline(outline, paint) }
            }
        }
    }
}
