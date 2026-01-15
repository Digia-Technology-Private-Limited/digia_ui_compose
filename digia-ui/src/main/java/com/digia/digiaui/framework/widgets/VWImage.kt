package com.digia.digiaui.framework.widgets

import LocalUIResources
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.UIResources
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.widgets.image.BlurHashDecoder
import com.digia.digiaui.init.DigiaUIManager
import java.net.URLEncoder

/** Image widget properties matching the Flutter schema */
data class ImageProps(
        val imageSrc: ExprOr<String>? = null,
        val sourceType: String = "network",
        val imageType: String = "image",
        val fit: String = "contain",
        val alignment: String = "center",
        val svgColor: ExprOr<String>? = null,
        val aspectRatio: ExprOr<Double>? = null,
        val placeholder: String = "none", // "none" or "blurHash"
        val placeholderSrc: String? = null // BlurHash string like "LEHV6nWB2yk8..."
) {
    companion object {
        fun fromJson(json: JsonLike): ImageProps {
            val srcMap = json["src"] as? Map<*, *>
            val rawImageSrc = srcMap?.get("imageSrc") ?: json["imageSrc"]
            val sourceType = (srcMap?.get("_sourceType") as? String) ?: "network"

            return ImageProps(
                    imageSrc = ExprOr.fromValue(rawImageSrc),
                    sourceType = sourceType,
                    imageType = (json["imageType"] as? String) ?: "image",
                    fit = (json["fit"] as? String) ?: "contain",
                    alignment = (json["alignment"] as? String) ?: "center",
                    svgColor = ExprOr.fromValue(json["svgColor"]),
                    aspectRatio = ExprOr.fromValue(json["aspectRatio"]),
                    placeholder = (json["placeholder"] as? String) ?: "none",
                    placeholderSrc = json["placeholderSrc"] as? String
            )
        }
    }
}

/** Virtual Widget for Image rendering */
class VWImage(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: ImageProps
) :
        VirtualLeafNode<ImageProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        val context = LocalContext.current
        val resources = LocalUIResources.current

        val imageSrc: String? =
                props.imageSrc?.let { exprOr ->
                    if (exprOr.isExpr) payload.evalExpr(exprOr)
                    else exprOr.value as? String ?: exprOr.value.toString()
                }

        val svgColor = payload.evalColor(props.svgColor?.value)
        val aspectRatio = payload.evalExpr(props.aspectRatio)?.toFloat()

        var modifier = Modifier.buildModifier(payload)
        if (aspectRatio != null && aspectRatio > 0f) {
            modifier = modifier.aspectRatio(aspectRatio)
        }

        if (imageSrc.isNullOrEmpty()) {
            RenderEmpty(modifier)
            return
        }

        val finalUrl = resolveImageUrl(imageSrc, props.sourceType, resources)

        if (finalUrl == null) {
            resources.images?.get(imageSrc)?.let {
                RenderPreloadedImage(it, modifier, props)
                return
            }
            RenderAssetPlaceholder(imageSrc, modifier)
            return
        }

        RenderNetworkImage(context, finalUrl, imageSrc, modifier, props, svgColor)
    }

    private fun resolveImageUrl(
            imageSrc: String,
            sourceType: String,
            resources: UIResources
    ): String? {
        if (imageSrc.startsWith("http://") || imageSrc.startsWith("https://")) {
            return applyProxyIfNeeded(imageSrc)
        }
        if (sourceType == "asset") return null
        return null
    }

    private fun applyProxyIfNeeded(url: String): String {
        val host = DigiaUIManager.getInstance().host
        return if (host?.resourceProxyUrl != null) {
            "${host.resourceProxyUrl}${URLEncoder.encode(url, "UTF-8")}"
        } else url
    }
}

// ============== Render Functions ==============

@Composable
private fun RenderNetworkImage(
        context: android.content.Context,
        finalUrl: String,
        imageSrc: String,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    val isSvg =
            props.imageType == "svg" ||
                    (props.imageType == "auto" && imageSrc.endsWith(".svg", ignoreCase = true))

    val contentScale = props.fit.toContentScale()
    val alignment = props.alignment.toAlignment()

    // Pre-decode BlurHash placeholder (cached via remember)
    val blurHashBitmap: Bitmap? =
            remember(props.placeholderSrc) {
                if (props.placeholder == "blurHash" && !props.placeholderSrc.isNullOrEmpty()) {
                    BlurHashDecoder.decode(props.placeholderSrc)
                } else null
            }

    val imageLoader =
            remember(isSvg) {
                ImageLoader.Builder(context)
                        .components { if (isSvg) add(SvgDecoder.Factory()) }
                        .build()
            }

    val imageRequest =
            remember(finalUrl) {
                ImageRequest.Builder(context).data(finalUrl).crossfade(300).build()
            }

    // Flutter Image behavior: Image widget fills available width for ALL BoxFit modes.
    // ContentScale determines how the content is scaled within that space.
    // For none/scaleDown, content may overflow so we clip.
    val needsClipping = props.fit == "none" || props.fit == "scaleDown"
    val imageModifier =
            if (needsClipping) {
                modifier.fillMaxWidth().clipToBounds()
            } else {
                modifier.fillMaxWidth()
            }

    SubcomposeAsyncImage(
            model = imageRequest,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = imageModifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = if (isSvg && svgColor != null) ColorFilter.tint(svgColor) else null,
            loading = {
                if (blurHashBitmap != null) {
                    Image(
                            bitmap = blurHashBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = contentScale,
                            alignment = alignment
                    )
                } else {
                    RenderLoading()
                }
            },
            error = { RenderError("Failed to load image", Modifier.fillMaxWidth()) },
            success = { SubcomposeAsyncImageContent() }
    )
}

@Composable
private fun RenderEmpty(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (DigiaUIManager.getInstance().host != null) {
            Text("No image source", color = Color.Gray)
        }
    }
}

@Composable
private fun RenderLoading() {
    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
        Text("Loading...", color = Color.Gray)
    }
}

@Composable
private fun RenderError(message: String, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(message, color = Color.Red)
    }
}

@Composable
private fun RenderAssetPlaceholder(assetPath: String, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Asset: $assetPath\n(Not available)", color = Color.Gray)
    }
}

@Composable
private fun RenderPreloadedImage(image: ImageBitmap, modifier: Modifier, props: ImageProps) {
    val contentScale = props.fit.toContentScale()
    val alignment = props.alignment.toAlignment()
    val needsClipping = props.fit == "none" || props.fit == "scaleDown"

    val imageModifier =
            if (needsClipping) {
                modifier.fillMaxWidth().clipToBounds()
            } else {
                modifier.fillMaxWidth()
            }

    Image(
            bitmap = image,
            contentDescription = null,
            modifier = imageModifier,
            contentScale = contentScale,
            alignment = alignment
    )
}

// ============== Extensions ==============

private fun String.toContentScale(): ContentScale =
        when (this) {
            "cover" -> ContentScale.Crop
            "fill" -> ContentScale.FillBounds
            "fitWidth" -> ContentScale.FillWidth
            "fitHeight" -> ContentScale.FillHeight
            "none" -> ContentScale.None
            "scaleDown" -> ContentScale.Inside
            else -> ContentScale.Fit
        }

private fun String.toAlignment(): Alignment =
        when (this) {
            "topLeft", "topStart" -> Alignment.TopStart
            "topCenter" -> Alignment.TopCenter
            "topRight", "topEnd" -> Alignment.TopEnd
            "centerLeft", "centerStart" -> Alignment.CenterStart
            "centerRight", "centerEnd" -> Alignment.CenterEnd
            "bottomLeft", "bottomStart" -> Alignment.BottomStart
            "bottomCenter" -> Alignment.BottomCenter
            "bottomRight", "bottomEnd" -> Alignment.BottomEnd
            else -> Alignment.Center
        }

/** Builder function for VWImage widget */
fun imageBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    return VWImage(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.parentProps,
            props = ImageProps.fromJson(data.props.value)
    )
}
