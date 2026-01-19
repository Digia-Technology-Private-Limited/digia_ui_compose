package com.digia.digiaui.framework.widgets.story

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.story.LocalStoryVideoCallback

private const val TAG = "StoryVideoPlayer"

/**
 * Virtual Story Video Player widget. Mirrors Flutter's VWStoryVideoPlayer +
 * InternalStoryVideoPlayer
 *
 * Key behaviors:
 * - Creates ExoPlayer for video playback
 * - Notifies StoryPresenter via LocalStoryVideoCallback when ready
 * - Handles auto-play and looping
 * - Disposes player on unmount
 */
class VWStoryVideoPlayer(
        refName: String? = null,
        commonProps: CommonProps? = null,
        parent: VirtualNode? = null,
        parentProps: Props? = null,
        props: StoryVideoPlayerProps
) :
        VirtualLeafNode<StoryVideoPlayerProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Debug: Log raw props
        Log.d(TAG, "=== StoryVideoPlayer Render ===")
        Log.d(TAG, "Raw props.videoUrl: ${props.videoUrl}")

        val videoUrl = payload.evalExpr(props.videoUrl)
        Log.d(TAG, "Evaluated videoUrl: $videoUrl")

        if (videoUrl.isNullOrBlank()) {
            Log.w(TAG, "Video URL is null or blank, returning Empty()")
            Empty()
            return
        }

        val autoPlay = payload.evalExpr(props.autoPlay) ?: true
        val looping = payload.evalExpr(props.looping) ?: false
        val fit = payload.evalExpr(props.fit) ?: "cover"

        Log.d(TAG, "autoPlay=$autoPlay, looping=$looping, fit=$fit")

        val context = LocalContext.current
        val onVideoLoad = LocalStoryVideoCallback.current

        Log.d(TAG, "onVideoLoad callback present: ${onVideoLoad != null}")

        var isInitialized by remember { mutableStateOf(false) }

        // Create ExoPlayer
        val exoPlayer =
                remember(videoUrl) {
                    Log.d(TAG, "Creating ExoPlayer for URL: $videoUrl")
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(videoUrl))
                        repeatMode = if (looping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                        Log.d(TAG, "ExoPlayer created, preparing...")
                        prepare()
                    }
                }

        // Notify presenter that video is loading
        LaunchedEffect(videoUrl) {
            Log.d(TAG, "LaunchedEffect: Signaling video loading")
            onVideoLoad?.invoke(null) // Signal loading
        }

        // Notify presenter when ready and handle auto-play
        LaunchedEffect(exoPlayer) {
            Log.d(TAG, "LaunchedEffect: Adding player listener")
            // Wait for player to be ready
            val listener =
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            val stateName =
                                    when (playbackState) {
                                        Player.STATE_IDLE -> "IDLE"
                                        Player.STATE_BUFFERING -> "BUFFERING"
                                        Player.STATE_READY -> "READY"
                                        Player.STATE_ENDED -> "ENDED"
                                        else -> "UNKNOWN($playbackState)"
                                    }
                            Log.d(
                                    TAG,
                                    "onPlaybackStateChanged: $stateName, isInitialized=$isInitialized"
                            )

                            if (playbackState == Player.STATE_READY && !isInitialized) {
                                isInitialized = true
                                Log.d(TAG, "Video READY! Duration: ${exoPlayer.duration}ms")
                                onVideoLoad?.invoke(exoPlayer)
                                if (autoPlay) {
                                    Log.d(TAG, "Auto-playing video...")
                                    exoPlayer.play()
                                }
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            Log.e(TAG, "Player error: ${error.message}", error)
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Log.d(TAG, "isPlaying changed: $isPlaying")
                        }
                    }
            exoPlayer.addListener(listener)
        }

        // Cleanup
        DisposableEffect(Unit) {
            onDispose {
                Log.d(TAG, "Disposing ExoPlayer")
                exoPlayer.release()
            }
        }

        // Build modifier
        var modifier = Modifier.buildModifier(payload)

        // Render video
        val resizeMode =
                when (fit.lowercase()) {
                    "cover" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    "contain" -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    "fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }

        Log.d(TAG, "Rendering PlayerView with resizeMode=$resizeMode")

        AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { ctx ->
                    Log.d(TAG, "Creating PlayerView")
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // No controls for story videos
                        setResizeMode(resizeMode)
                        layoutParams =
                                ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    view.setResizeMode(resizeMode)
                }
        )
    }
}

/** Builder function for StoryVideoPlayer widget registration. */
fun storyVideoPlayerBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        registry: VirtualWidgetRegistry
): VirtualNode {
    Log.d(TAG, "storyVideoPlayerBuilder called, props: ${data.props.value}")
    return VWStoryVideoPlayer(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.parentProps,
            props = StoryVideoPlayerProps.fromJson(data.props.value)
    )
}
