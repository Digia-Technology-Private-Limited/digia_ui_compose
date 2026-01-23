package com.digia.digiaui.framework.internals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.net.URLDecoder

@Composable
fun InternalYoutubePlayer(
    videoUrl: String,
    isMuted: Boolean = false,
    loop: Boolean = false,
    autoPlay: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoId = remember(videoUrl) { extractVideoId(videoUrl) }

    var currentPlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var currentView by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var lastVideoId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            currentPlayer = null
            lastVideoId = null
            currentView?.let { view ->
                lifecycleOwner.lifecycle.removeObserver(view)
                view.release()
            }
            currentView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            val view = YouTubePlayerView(context).also {
                currentView = it
                lifecycleOwner.lifecycle.addObserver(it)

                it.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        currentPlayer = youTubePlayer

                        if (videoId.isNotBlank()) {
                            lastVideoId = videoId
                            if (autoPlay) youTubePlayer.loadVideo(videoId, 0f) else youTubePlayer.cueVideo(videoId, 0f)
                        }

                        if (isMuted) youTubePlayer.mute() else youTubePlayer.unMute()
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        if (loop && state == PlayerConstants.PlayerState.ENDED && videoId.isNotBlank()) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    }
                })
            }

            view
        },
        update = {
            val player = currentPlayer
            if (player != null) {
                if (isMuted) player.mute() else player.unMute()

                if (videoId.isNotBlank() && videoId != lastVideoId) {
                    lastVideoId = videoId
                    if (autoPlay) player.loadVideo(videoId, 0f) else player.cueVideo(videoId, 0f)
                }
            }
        }
    )
}

private fun extractVideoId(url: String): String {
    if (url.isBlank()) return ""
    if (!url.contains("http", ignoreCase = true) && !url.contains("www", ignoreCase = true)) {
        return url
    }

    return try {
        val decoded = URLDecoder.decode(url, "UTF-8")
        val uri = android.net.Uri.parse(decoded)
        uri.getQueryParameter("v") ?: (uri.lastPathSegment ?: "")
    } catch (_: Throwable) {
        ""
    }
}
