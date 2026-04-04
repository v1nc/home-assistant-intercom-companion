package io.homeassistant.companion.android.util.compose.webview

import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import io.homeassistant.companion.android.chromium.BundledWebView
import io.homeassistant.companion.android.common.data.HomeAssistantApis
import timber.log.Timber

const val BLANK_URL = "about:blank"

@VisibleForTesting const val HA_WEBVIEW_TAG = "ha_web_view_tag"

/**
 * A composable that displays a bundled Chromium WebView specifically configured for Home Assistant.
 * This WebView includes default settings for Home Assistant, such as:
 * - Javascript/dom storage enabled
 * - Zoom controls disabled
 * - Custom user agent
 * - Transparent background
 *
 * This composable provides a convenient way to embed a WebView within a Jetpack Compose UI.
 * Further customization of the WebView instance is possible through the [configure] lambda.
 *
 * The WebView will be sized to match the [modifier].
 *
 * If the bundled Chromium WebView fails to initialize, the [onWebViewCreationFailed] callback
 * is invoked with the exception and a placeholder view is shown instead.
 *
 * @param onWebViewCreationFailed Called when the WebView fails to initialize.
 * @param modifier The modifier to be applied to this WebView.
 * @param configure A lambda that allows for customization of the BundledWebView instance.
 * @param factory A lambda that creates the BundledWebView instance. If this returns null, a new
 *                BundledWebView will be created with the current context.
 */
@Composable
fun HAWebView(
    onWebViewCreationFailed: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    configure: BundledWebView.() -> Unit = {},
    factory: () -> BundledWebView? = { null },
    // Only used when the backstack of the webView is empty
    onBackPressed: (() -> Unit)? = null,
) {
    var webview by remember { mutableStateOf<BundledWebView?>(null) }
    val modifier = modifier.testTag(HA_WEBVIEW_TAG)

    // In preview/screenshot mode, show a placeholder instead of WebView
    // to avoid having issue with screenshots.
    if (LocalInspectionMode.current) {
        Text(
            text = "WebviewPlaceholder",
            modifier = modifier,
        )
    } else {
        AndroidView(
            factory = { context ->
                try {
                    (factory() ?: BundledWebView(context)).apply {
                        webview = this
                        // We want the modifier to determine the size so the WebView should match the parent
                        this.layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        )
                        defaultSettings()
                        configure(this)
                    }
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to create BundledWebView")
                    onWebViewCreationFailed(t)
                    // AndroidView requires a non-null View; return an empty placeholder
                    FrameLayout(context)
                }
            },
            modifier = modifier,
            onRelease = {
                Timber.d("onRelease WebView, stopping loading")
                (it as? BundledWebView)?.stopLoading()
                webview = null
            },
        )
    }

    // To avoid checking doUpdateVisitedHistory from the webViewClient we simply delegate the back button handling
    // to the webView and when the webview backstack is empty we call the callback given in parameter that should be
    // handle by the navHost.
    BackHandler(onBackPressed != null) {
        webview.takeIf { it?.canGoBack() == true }?.goBack() ?: onBackPressed?.invoke()
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun BundledWebView.defaultSettings() {
    bundledSettings.apply {
        // https://github.com/home-assistant/android/pull/3353
        minimumFontSize = 5
        javaScriptEnabled = true
        domStorageEnabled = true
        // https://github.com/home-assistant/android/pull/2252
        displayZoomControls = false
        userAgentString += " ${HomeAssistantApis.USER_AGENT_STRING}"
    }
    // Set WebView background color to transparent, so that the theme of the android activity has control over it.
    // This enables the ability to have the launch screen behind the WebView until the web frontend gets rendered
    setBackgroundColor(Color.TRANSPARENT)
}
