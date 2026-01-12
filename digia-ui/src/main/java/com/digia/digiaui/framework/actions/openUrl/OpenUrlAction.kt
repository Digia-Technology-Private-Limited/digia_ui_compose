package com.digia.digiaui.framework.actions.openUrl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.platform.LocalUriHandler
import com.digia.digiaui.framework.actions.base.Action
import com.digia.digiaui.framework.actions.base.ActionId
import com.digia.digiaui.framework.actions.base.ActionProcessor
import com.digia.digiaui.framework.actions.base.ActionType
import com.digia.digiaui.framework.expr.ScopeContext
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.utils.JsonLike
import androidx.core.net.toUri
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.UIResources
import com.digia.digiaui.framework.state.StateContext


enum class LaunchMode {
    IN_APP_WEBVIEW,
    EXTERNAL_APPLICATION,
    EXTERNAL_NON_BROWSER_APPLICATION,
    PLATFORM_DEFAULT
}

fun uriLaunchMode(value: Any?): LaunchMode =
    when (value) {
        "inAppWebView", "inApp" ->
            LaunchMode.IN_APP_WEBVIEW

        "externalApplication", "external" ->
            LaunchMode.EXTERNAL_APPLICATION

        "externalNonBrowserApplication" ->
            LaunchMode.EXTERNAL_NON_BROWSER_APPLICATION

        else ->
            LaunchMode.PLATFORM_DEFAULT
    }

/**
 * OpenUrl Action
 * 
 * Opens a URL in an external browser or appropriate app.
 * 
 * @param url The URL to open (can be an expression)
 * @param launchMode How to launch the URL (default: EXTERNAL_APPLICATION)
 */
data class OpenUrlAction(
    override var actionId: ActionId? = null,
    override var disableActionIf: ExprOr<Boolean>? = null,
    val url: ExprOr<String>?,
    val launchMode: String? = null
) : Action {
    override val actionType = ActionType.OPEN_URL

    override fun toJson(): JsonLike =
        mapOf(
            "type" to actionType.value,
            "url" to url?.toJson(),
            "launchMode" to launchMode
        )

    companion object {
        fun fromJson(json: JsonLike): OpenUrlAction {
            return OpenUrlAction(
                url = ExprOr.fromValue(json["url"]),
                launchMode = json["launchMode"] as String?
            )
        }
    }
}

/** Processor for open URL action */
class OpenUrlProcessor(
    private val urlLauncher: UrlLauncher = AndroidUrlLauncher()
) : ActionProcessor<OpenUrlAction>() {

    override fun execute(
        context: Context,
        action: OpenUrlAction,
        scopeContext: ScopeContext?,
        stateContext: StateContext?,
        resourceProvider: UIResources?,
        id: String
    ): Any? {
        val url = action.url?.evaluate<String>(scopeContext).orEmpty()
        if (url.isBlank()) return false

        val launchMode = uriLaunchMode(action.launchMode)

        try {
            urlLauncher.open(context, url, launchMode)
            return  true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}



interface UrlLauncher {
    fun open(
        context: Context,
        url: String,
        mode: LaunchMode
    )
}

class AndroidUrlLauncher : UrlLauncher {

    override fun open(context: Context, url: String, mode: LaunchMode) {
        when (mode) {
            LaunchMode.IN_APP_WEBVIEW -> openCustomTab(context, url)
            LaunchMode.EXTERNAL_APPLICATION -> openExternal(context, url)
            LaunchMode.EXTERNAL_NON_BROWSER_APPLICATION -> openNonBrowser(context, url)
            LaunchMode.PLATFORM_DEFAULT -> openExternal(context, url)
        }
    }

    private fun openExternal(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openNonBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        context.startActivity(intent)
    }

    private fun openCustomTab(context: Context, url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(context, url.toUri())
    }
}

