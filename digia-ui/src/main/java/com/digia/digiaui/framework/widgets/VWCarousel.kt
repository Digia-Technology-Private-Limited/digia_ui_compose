package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.evalColor
import com.digia.digiaui.framework.expr.DefaultScopeContext
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.toDp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/** Carousel indicator properties */
data class CarouselIndicatorProps(
    val showIndicator: Boolean? = null,
    val offset: Double? = null,
    val dotHeight: Double? = null,
    val dotWidth: Double? = null,
    val spacing: Double? = null,
    val dotColor: String? = null,
    val activeDotColor: String? = null,
    val indicatorEffectType: String? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): CarouselIndicatorProps? {
            if (json == null) return null
            return CarouselIndicatorProps(
                showIndicator = json["showIndicator"] as? Boolean,
                offset = (json["offset"] as? Number)?.toDouble(),
                dotHeight = (json["dotHeight"] as? Number)?.toDouble(),
                dotWidth = (json["dotWidth"] as? Number)?.toDouble(),
                spacing = (json["spacing"] as? Number)?.toDouble(),
                dotColor = json["dotColor"] as? String,
                activeDotColor = json["activeDotColor"] as? String,
                indicatorEffectType = json["indicatorEffectType"] as? String
            )
        }
    }
}

/** Carousel widget properties */
data class CarouselProps(
    val height: ExprOr<String>? = null,
    val width: ExprOr<String>? = null,
    val dataSource: Any? = null,
    val direction: ExprOr<String>? = null,
    val aspectRatio: ExprOr<Double>? = null,
    val initialPage: ExprOr<Int>? = null,
    val padEnds: ExprOr<Boolean>? = null,
    val enlargeCenterPage: ExprOr<Boolean>? = null,
    val enlargeFactor: ExprOr<Double>? = null,
    val viewportFraction: ExprOr<Double>? = null,
    val keepAlive: ExprOr<Boolean>? = null,
    val autoPlay: ExprOr<Boolean>? = null,
    val animationDuration: ExprOr<Int>? = null,
    val autoPlayInterval: ExprOr<Int>? = null,
    val pageSnapping: ExprOr<Boolean>? = null,
    val infiniteScroll: ExprOr<Boolean>? = null,
    val reverseScroll: ExprOr<Boolean>? = null,
    val indicator: JsonLike? = null,
    val onChanged: JsonLike? = null
) {
    companion object {
        fun fromJson(json: JsonLike): CarouselProps {
            return CarouselProps(
                height = ExprOr.fromValue(json["height"]),
                width = ExprOr.fromValue(json["width"]),
                dataSource = json["dataSource"],
                direction = ExprOr.fromValue(json["direction"]),
                aspectRatio = ExprOr.fromValue(json["aspectRatio"]),
                initialPage = ExprOr.fromValue(json["initialPage"]),
                padEnds = ExprOr.fromValue(json["padEnds"]),
                enlargeCenterPage = ExprOr.fromValue(json["enlargeCenterPage"]),
                enlargeFactor = ExprOr.fromValue(json["enlargeFactor"]),
                viewportFraction = ExprOr.fromValue(json["viewportFraction"]),
                keepAlive = ExprOr.fromValue(json["keepAlive"]),
                autoPlay = ExprOr.fromValue(json["autoPlay"]),
                animationDuration = ExprOr.fromValue(json["animationDuration"]),
                autoPlayInterval = ExprOr.fromValue(json["autoPlayInterval"]),
                pageSnapping = ExprOr.fromValue(json["pageSnapping"]),
                infiniteScroll = ExprOr.fromValue(json["infiniteScroll"]),
                reverseScroll = ExprOr.fromValue(json["reverseScroll"]),
                indicator = json["indicator"] as? JsonLike,
                onChanged = json["onChanged"] as? JsonLike
            )
        }
    }
}

/**
 * Virtual Carousel widget
 *
 * Renders a carousel/slider with items from a data source.
 * Each item is rendered using the child template widget.
 * Supports indicators, auto-play, and various customization options.
 */
@OptIn(ExperimentalFoundationApi::class)
class VWCarousel(
    refName: String? = null,
    commonProps: CommonProps? = null,
    props: CarouselProps,
    parent: VirtualNode? = null,
    slots: Map<String, List<VirtualNode>>? = null,
    parentProps: Props? = null
) : VirtualCompositeNode<CarouselProps>(
    props = props,
    commonProps = commonProps,
    parentProps = parentProps,
    parent = parent,
    refName = refName,
    slots = slots
) {

    private val shouldRepeatChild: Boolean
        get() = props.dataSource != null

    @Composable
    override fun Render(payload: RenderPayload) {
        if (child == null || !shouldRepeatChild) {
            Empty()
            return
        }

        val items = payload.eval<List<Any>>(props.dataSource) ?: emptyList()
        if (items.isEmpty()) {
            Empty()
            return
        }

        val width = payload.evalExpr(props.width)
        val height = payload.evalExpr(props.height)
        val direction = payload.evalExpr(props.direction) ?: "horizontal"
        val aspectRatio = payload.evalExpr(props.aspectRatio) ?: 0.25
        val initialPage = (payload.evalExpr(props.initialPage) ?: 0).coerceIn(0, items.size - 1)
        val padEnds = payload.evalExpr(props.padEnds) ?: true
        val enlargeCenterPage = payload.evalExpr(props.enlargeCenterPage) ?: false
        val enlargeFactor = payload.evalExpr(props.enlargeFactor) ?: 0.3
        val viewportFraction = payload.evalExpr(props.viewportFraction) ?: 0.8
        val autoPlay = payload.evalExpr(props.autoPlay) ?: false
        @Suppress("UNUSED_VARIABLE")
        val animationDuration = payload.evalExpr(props.animationDuration) ?: 800
        val autoPlayInterval = payload.evalExpr(props.autoPlayInterval) ?: 1600
        val pageSnapping = payload.evalExpr(props.pageSnapping) ?: true
        val infiniteScroll = payload.evalExpr(props.infiniteScroll) ?: false
        val reverseScroll = payload.evalExpr(props.reverseScroll) ?: false

        @Suppress("UNCHECKED_CAST")
        val indicatorAvailable = props.indicator?.get("indicatorAvailable") as? JsonLike
        val indicatorProps = CarouselIndicatorProps.fromJson(indicatorAvailable)
        val showIndicator = indicatorProps?.showIndicator ?: false

        val pageCount = if (infiniteScroll) Int.MAX_VALUE else items.size
        val pagerState = rememberPagerState(
            initialPage = if (infiniteScroll) Int.MAX_VALUE / 2 + initialPage else initialPage,
            pageCount = { pageCount }
        )

        // Auto-play effect with animation duration
        if (autoPlay) {
            LaunchedEffect(pagerState.currentPage) {
                delay(autoPlayInterval.toLong())
                val nextPage = if (reverseScroll) {
                    pagerState.currentPage - 1
                } else {
                    pagerState.currentPage + 1
                }
                if (infiniteScroll || nextPage < items.size) {
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }

        // Handle onChanged callback
        LaunchedEffect(pagerState.currentPage) {
            @Suppress("UNUSED_VARIABLE")
            val actualPage = if (infiniteScroll) {
                pagerState.currentPage % items.size
            } else {
                pagerState.currentPage
            }
            props.onChanged?.let {
                payload.executeAction(it, "onChanged")
            }
        }

        // Build modifier with size constraints
        var modifier: Modifier = Modifier
        if (width != null) {
            modifier = modifier.width(width.toDp() ?: 300.dp)
        }
        if (height != null) {
            modifier = modifier.height(height.toDp() ?: 200.dp)
        } else {
            modifier = modifier.aspectRatio(aspectRatio.toFloat())
        }

        // Main carousel content with direction support
        val carouselContent = @Composable {
            if (direction == "vertical") {
                VerticalPager(
                    state = pagerState,
                    modifier = modifier,
                    pageSize = if (height != null) {
                        PageSize.Fixed((height.toDp() ?: 300.dp) * viewportFraction.toFloat())
                    } else {
                        PageSize.Fill
                    },
                    pageSpacing = if (padEnds) 8.dp else 0.dp,
                    reverseLayout = reverseScroll,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = pageSnapping
                ) { page ->
                    CarouselPage(
                        page = page,
                        items = items,
                        infiniteScroll = infiniteScroll,
                        enlargeCenterPage = enlargeCenterPage,
                        enlargeFactor = enlargeFactor,
                        pagerState = pagerState,
                        payload = payload,
                        child = child,
                        createExprContext = ::createExprContext
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = modifier,
                    pageSize = PageSize.Fixed((width?.toDp() ?: 300.dp) * viewportFraction.toFloat()),
                    pageSpacing = if (padEnds) 8.dp else 0.dp,
                    reverseLayout = reverseScroll,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = pageSnapping
                ) { page ->
                    CarouselPage(
                        page = page,
                        items = items,
                        infiniteScroll = infiniteScroll,
                        enlargeCenterPage = enlargeCenterPage,
                        enlargeFactor = enlargeFactor,
                        pagerState = pagerState,
                        payload = payload,
                        child = child,
                        createExprContext = ::createExprContext
                    )
                }
            }
        }

        // Wrap with indicator if enabled
        if (showIndicator && indicatorProps != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                carouselContent()
                Spacer(modifier = Modifier.height(8.dp))
                CarouselIndicator(
                    pagerState = pagerState,
                    itemCount = items.size,
                    infiniteScroll = infiniteScroll,
                    indicatorProps = indicatorProps,
                    payload = payload
                )
            }
        } else {
            carouselContent()
        }
    }

    private fun createExprContext(item: Any?, index: Int): DefaultScopeContext {
        val carouselObj = mapOf("currentItem" to item, "index" to index)
        val variables = mutableMapOf<String, Any?>().apply {
            putAll(carouselObj)
            refName?.let { name -> put(name, carouselObj) }
        }
        return DefaultScopeContext(variables = variables)
    }
}

/**
 * Extracted page content composable to avoid duplication
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselPage(
    page: Int,
    items: List<Any>,
    infiniteScroll: Boolean,
    enlargeCenterPage: Boolean,
    enlargeFactor: Double,
    pagerState: PagerState,
    payload: RenderPayload,
    child: VirtualNode?,
    createExprContext: (Any?, Int) -> DefaultScopeContext
) {
    val actualIndex = if (infiniteScroll) page % items.size else page
    val item = items[actualIndex]

    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
    val scale = if (enlargeCenterPage) {
        lerp(
            start = 1f - enlargeFactor.toFloat(),
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        val scopedPayload = payload.copyWithChainedContext(
            createExprContext(item, actualIndex)
        )
        child?.ToWidget(scopedPayload)
    }
}

/**
 * Carousel indicator composable
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselIndicator(
    pagerState: PagerState,
    itemCount: Int,
    infiniteScroll: Boolean,
    indicatorProps: CarouselIndicatorProps,
    payload: RenderPayload
) {
    val dotHeight = (indicatorProps.dotHeight ?: 10.0).dp
    val dotWidth = (indicatorProps.dotWidth ?: 10.0).dp
    val spacing = (indicatorProps.spacing ?: 4.0).dp
    val dotColor = payload.evalColor(indicatorProps.dotColor) ?: Color.Gray
    val activeDotColor = payload.evalColor(indicatorProps.activeDotColor) ?: Color.Blue
    val effectType = indicatorProps.indicatorEffectType ?: "expanding"

    val currentPage = if (infiniteScroll) {
        pagerState.currentPage % itemCount
    } else {
        pagerState.currentPage
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(itemCount) { index ->
            val isSelected = index == currentPage
            
            when (effectType) {
                "expanding" -> {
                    Box(
                        modifier = Modifier
                            .height(dotHeight)
                            .width(if (isSelected) dotWidth * 2 else dotWidth)
                            .clip(CircleShape)
                            .background(if (isSelected) activeDotColor else dotColor)
                    )
                }
                "scale" -> {
                    val scale = if (isSelected) 1.3f else 1f
                    Box(
                        modifier = Modifier
                            .size(dotWidth * scale, dotHeight * scale)
                            .clip(CircleShape)
                            .background(if (isSelected) activeDotColor else dotColor)
                    )
                }
                "worm" -> {
                    Box(
                        modifier = Modifier
                            .height(dotHeight)
                            .width(if (isSelected) dotWidth * 2.5f else dotWidth)
                            .clip(CircleShape)
                            .background(if (isSelected) activeDotColor else dotColor)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(dotWidth, dotHeight)
                            .clip(CircleShape)
                            .background(if (isSelected) activeDotColor else dotColor)
                    )
                }
            }
        }
    }
}

/** Builder function for Carousel widget */
fun carouselBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData = data.childGroups?.mapValues { (_, childrenData) ->
        childrenData.map { data -> registry.createWidget(data, parent) }
    }

    return VWCarousel(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.parentProps,
        props = CarouselProps.fromJson(data.props.value),
        slots = childrenData
    )
}
