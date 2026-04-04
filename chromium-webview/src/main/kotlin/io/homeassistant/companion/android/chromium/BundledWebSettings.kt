package io.homeassistant.companion.android.chromium

import org.chromium.android_webview.AwSettings

/**
 * Wraps Chromium's [AwSettings] to provide an API surface compatible with
 * [android.webkit.WebSettings] for the settings actually used by the app.
 *
 * This avoids requiring callers to depend on internal Chromium types.
 */
class BundledWebSettings internal constructor(private val awSettings: AwSettings) {

    /** Whether JavaScript is enabled. Default: false. */
    var javaScriptEnabled: Boolean
        get() = awSettings.javaScriptEnabled
        set(value) {
            awSettings.javaScriptEnabled = value
        }

    /** Whether DOM storage API is enabled. Default: false. */
    var domStorageEnabled: Boolean
        get() = awSettings.domStorageEnabled
        set(value) {
            awSettings.domStorageEnabled = value
        }

    /** Whether the WebView should display on-screen zoom controls. */
    var displayZoomControls: Boolean = false

    /** Whether the WebView should use its built-in zoom mechanisms. */
    var builtInZoomControls: Boolean
        get() = awSettings.builtInZoomControls
        set(value) {
            awSettings.builtInZoomControls = value
        }

    /** The user-agent string used by the WebView. */
    var userAgentString: String
        get() = awSettings.userAgentString
        set(value) {
            awSettings.userAgentString = value
        }

    /** The minimum logical font size. */
    var minimumFontSize: Int
        get() = awSettings.minimumFontSize
        set(value) {
            awSettings.minimumFontSize = value
        }

    /** Whether the WebView should not load image resources from the network. */
    var blockNetworkImage: Boolean
        get() = awSettings.imagesEnabled.not()
        set(value) {
            awSettings.imagesEnabled = !value
        }

    /** Whether the WebView requires a user gesture to play media. */
    var mediaPlaybackRequiresUserGesture: Boolean
        get() = awSettings.mediaPlaybackRequiresUserGesture
        set(value) {
            awSettings.mediaPlaybackRequiresUserGesture = value
        }

    /** The text zoom of the page in percent. */
    var textZoom: Int
        get() = awSettings.textZoom
        set(value) {
            awSettings.textZoom = value
        }
}
