package com.digia.digiaui.framework.widgets

import LocalUIResources
import android.content.Context
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

/**
 * Sealed class representing the resolved image source. Eliminates string comparisons and provides
 * type-safe handling.
 */
sealed class ImageSource {
    data class Network(val url: String) : ImageSource()
    data class LocalAsset(val path: String) : ImageSource()
    data class Preloaded(val bitmap: ImageBitmap) : ImageSource()
    object Empty : ImageSource()
}

/** Image widget properties matching the Flutter schema. */
data class ImageProps(
        val imageSrc: ExprOr<String>? = null,
        val sourceType: String = "network",
        val imageType: String = "image",
        val fit: String = "contain",
        val alignment: String = "center",
        val svgColor: ExprOr<String>? = null,
        val aspectRatio: ExprOr<Double>? = null,
        val placeholder: String = "none",
        val placeholderSrc: String? = null
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

/** Virtual Widget for Image rendering. */
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

        val imageSrc = resolveImageSrc(payload)
        val svgColor = payload.evalColor(props.svgColor?.value)
        val aspectRatio = payload.evalExpr(props.aspectRatio)?.toFloat()

        val modifier = buildImageModifier(payload, aspectRatio)

        val source = resolveImageSource(imageSrc, props.sourceType, resources)

        RenderImage(context, source, modifier, props, svgColor)
    }

    private fun resolveImageSrc(payload: RenderPayload): String? {
        val exprOr = props.imageSrc ?: return null

        if (exprOr.isExpr) {
            return payload.evalExpr(exprOr)
        }

        return exprOr.value as? String ?: exprOr.value.toString()
    }

    @Composable
    private fun buildImageModifier(payload: RenderPayload, aspectRatio: Float?): Modifier {
        var modifier = Modifier.buildModifier(payload)

        if (aspectRatio != null && aspectRatio > 0f) {
            modifier = modifier.aspectRatio(aspectRatio)
        }

        return modifier
    }
}

/** Resolves the image source based on source type and available resources. */
fun resolveImageSource(imageSrc: String?, sourceType: String, resources: UIResources): ImageSource {
    if (imageSrc.isNullOrEmpty()) {
        return ImageSource.Empty
    }

    val preloadedBitmap = resources.images?.get(imageSrc)
    if (preloadedBitmap != null) {
        return ImageSource.Preloaded(preloadedBitmap)
    }

    val isNetworkUrl = imageSrc.startsWith("http://") || imageSrc.startsWith("https://")
    if (isNetworkUrl) {
        val finalUrl = applyProxyIfNeeded(imageSrc)
        return ImageSource.Network(finalUrl)
    }

    // For both 'asset' and 'network' sourceTypes with non-URL imageSrc,
    // try to resolve from assetUrls first, then fall back to local asset
    val cloudUrl = resources.assetUrls?.get(imageSrc)
    if (cloudUrl != null) {
        val finalUrl = applyProxyIfNeeded(cloudUrl)
        return ImageSource.Network(finalUrl)
    }

    // Fall back to local asset for any non-URL imageSrc
    val cleanPath = imageSrc.removePrefix("/")
    return ImageSource.LocalAsset(cleanPath)
}

/**
 * Compatibility function for VWAvatar - resolves image source to a URL string. Returns the URL for
 * network/asset sources.
 */
internal fun resolveImageUrl(imageSrc: String, sourceType: String, resources: UIResources): String {
    val source = resolveImageSource(imageSrc, sourceType, resources)

    return when (source) {
        is ImageSource.Network -> source.url
        is ImageSource.LocalAsset -> "file:///android_asset/${source.path}"
        else -> imageSrc
    }
}

/** Applies resource proxy URL if configured. */
private fun applyProxyIfNeeded(url: String): String {
    val proxyUrl = DigiaUIManager.getInstance().host?.resourceProxyUrl

    if (proxyUrl == null) {
        return url
    }

    return proxyUrl + URLEncoder.encode(url, "UTF-8")
}

/** Unified image rendering function that handles all source types. */
@Composable
fun RenderImage(
        context: Context,
        source: ImageSource,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    when (source) {
        is ImageSource.Empty -> RenderEmptyImage(modifier)
        is ImageSource.Preloaded -> RenderPreloadedImage(source.bitmap, modifier, props)
        is ImageSource.Network -> RenderNetworkImage(context, source.url, modifier, props, svgColor)
        is ImageSource.LocalAsset ->
                RenderLocalAssetImage(context, source.path, modifier, props, svgColor)
    }
}

@Composable
internal fun RenderEmptyImage(modifier: Modifier) {
    val isDebugMode = DigiaUIManager.getInstance().host != null

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isDebugMode) {
            Text("No image source", color = Color.Gray)
        }
    }
}

@Composable internal fun RenderEmpty(modifier: Modifier) = RenderEmptyImage(modifier)

@Composable
internal fun RenderPreloadedImage(bitmap: ImageBitmap, modifier: Modifier, props: ImageProps) {
    val contentScale = props.fit.toContentScale()
    val alignment = props.alignment.toAlignment()
    val imageModifier = applyClippingIfNeeded(modifier, props.fit)

    Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = imageModifier,
            contentScale = contentScale,
            alignment = alignment
    )
}

@Composable
internal fun RenderNetworkImage(
        context: Context,
        url: String,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    RenderNetworkImageInternal(context, url, modifier, props, svgColor)
}

/** Compatibility overload for VWAvatar which passes imageSrc separately. */
@Composable
internal fun RenderNetworkImage(
        context: Context,
        url: String,
        imageSrc: String,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    RenderNetworkImageInternal(context, url, modifier, props, svgColor)
}

@Composable
private fun RenderNetworkImageInternal(
        context: Context,
        url: String,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    val isSvg = isSvgImage(url, props.imageType)
    val contentScale = props.fit.toContentScale()
    val alignment = props.alignment.toAlignment()
    val imageModifier = applyClippingIfNeeded(modifier, props.fit)

    val blurHashBitmap = rememberBlurHashBitmap(props)
    val imageLoader = rememberImageLoader(context, isSvg)
    val imageRequest = rememberImageRequest(context, url)
    val colorFilter = createColorFilter(isSvg, svgColor)

    SubcomposeAsyncImage(
            model = imageRequest,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = imageModifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
            loading = { RenderLoadingPlaceholder(blurHashBitmap, contentScale, alignment) },
            error = { RenderErrorImage() },
            success = { SubcomposeAsyncImageContent() }
    )
}

@Composable
internal fun RenderLocalAssetImage(
        context: Context,
        assetPath: String,
        modifier: Modifier,
        props: ImageProps,
        svgColor: Color?
) {
    val assetUri = "file:///android_asset/$assetPath"
    android.util.Log.d("VWImage", "Loading local asset: path=$assetPath, uri=$assetUri")
    RenderNetworkImage(context, assetUri, modifier, props, svgColor)
}

@Composable
private fun RenderLoadingPlaceholder(
        blurHashBitmap: Bitmap?,
        contentScale: ContentScale,
        alignment: Alignment
) {
    if (blurHashBitmap != null) {
        Image(
                bitmap = blurHashBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = contentScale,
                alignment = alignment
        )
        return
    }

    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
        Text("Loading...", color = Color.Gray)
    }
}

@Composable
private fun RenderErrorImage() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("Failed to load image", color = Color.Red)
    }
}

@Composable
private fun rememberBlurHashBitmap(props: ImageProps): Bitmap? {
    return remember(props.placeholderSrc) {
        val isBlurHash = props.placeholder == "blurHash"
        val hasSource = !props.placeholderSrc.isNullOrEmpty()

        if (isBlurHash && hasSource) {
            BlurHashDecoder.decode(props.placeholderSrc)
        } else {
            null
        }
    }
}

@Composable
private fun rememberImageLoader(context: Context, isSvg: Boolean): ImageLoader {
    return remember(isSvg) {
        ImageLoader.Builder(context).components { if (isSvg) add(SvgDecoder.Factory()) }.build()
    }
}

@Composable
private fun rememberImageRequest(context: Context, url: String): ImageRequest {
    return remember(url) { ImageRequest.Builder(context).data(url).crossfade(300).build() }
}

private fun isSvgImage(url: String, imageType: String): Boolean {
    if (imageType == "svg") return true
    if (imageType == "auto" && url.endsWith(".svg", ignoreCase = true)) return true
    return false
}

private fun createColorFilter(isSvg: Boolean, svgColor: Color?): ColorFilter? {
    if (!isSvg) return null
    if (svgColor == null) return null
    return ColorFilter.tint(svgColor)
}

private fun applyClippingIfNeeded(modifier: Modifier, fit: String): Modifier {
    val needsClipping = fit == "none" || fit == "scaleDown"

    return if (needsClipping) {
        modifier.fillMaxWidth().clipToBounds()
    } else {
        modifier.fillMaxWidth()
    }
}

private fun String.toContentScale(): ContentScale {
    return when (this) {
        "cover" -> ContentScale.Crop
        "fill" -> ContentScale.FillBounds
        "fitWidth" -> ContentScale.FillWidth
        "fitHeight" -> ContentScale.FillHeight
        "none" -> ContentScale.None
        "scaleDown" -> ContentScale.Inside
        else -> ContentScale.Fit
    }
}

private fun String.toAlignment(): Alignment {
    return when (this) {
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
}

/** Builder function for VWImage widget. */
fun imageBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    val imageProps = ImageProps.fromJson(data.props.value)

    return VWImage(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.parentProps,
            props = imageProps
    )
}
