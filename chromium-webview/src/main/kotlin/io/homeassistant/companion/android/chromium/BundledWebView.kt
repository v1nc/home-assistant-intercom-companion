package io.homeassistant.companion.android.chromium

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.chromium.android_webview.AwBrowserContext
import org.chromium.android_webview.AwContents
import org.chromium.android_webview.AwSettings
import timber.log.Timber

/**
 * A View that embeds a bundled Chromium rendering engine, providing an API
 * surface compatible with [android.webkit.WebView] for the methods used by
 * the Home Assistant Companion app.
 *
 * Unlike the system WebView, this view bundles a specific Chromium version
 * with the app, ensuring consistent behavior across devices regardless of
 * the system WebView version installed.
 *
 * Uses software rendering via [AwContents.onDraw] for maximum device compatibility.
 *
 * Must be created after [ChromiumInitializer.initialize] has been called.
 *
 * @see ChromiumInitializer
 */
@SuppressLint("ViewConstructor")
class BundledWebView(context: Context) : FrameLayout(context) {

    private val contentsClient = BundledContentsClient().apply {
        bundledWebView = this@BundledWebView
    }

    private val containerView: SoftwareDrawContainerView
    private val awContents: AwContents
    private val awSettings: AwSettings

    /** Provides access to WebView-compatible settings for this bundled WebView. */
    val bundledSettings: BundledWebSettings

    init {
        require(ChromiumInitializer.isInitialized()) {
            "ChromiumInitializer.initialize() must be called before creating a BundledWebView"
        }

        containerView = SoftwareDrawContainerView(context)

        val sharedPrefs: SharedPreferences = context.getSharedPreferences(
            "BundledWebViewPrefs",
            Context.MODE_PRIVATE,
        )
        val browserContext = AwBrowserContext(
            sharedPrefs,
            AwBrowserContext.getDefault().nativePointer,
            true,
        )

        awSettings = AwSettings(context, true, false, false, false, false).apply {
            javaScriptEnabled = false
            domStorageEnabled = false
        }

        awContents = AwContents(
            browserContext,
            containerView,
            context,
            containerView.internalAccessDelegate,
            containerView.nativeDrawFunctorFactory,
            contentsClient,
            awSettings,
        )

        containerView.initialize(awContents)
        addView(
            containerView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )

        bundledSettings = BundledWebSettings(awSettings)
    }

    /** Sets the [WebViewClient] that receives page lifecycle and error callbacks. */
    var webViewClient: WebViewClient?
        get() = contentsClient.webViewClient
        set(value) {
            contentsClient.webViewClient = value
        }

    /** Sets the [WebChromeClient] that receives UI-related callbacks. */
    var webChromeClient: WebChromeClient?
        get() = contentsClient.webChromeClient
        set(value) {
            contentsClient.webChromeClient = value
        }

    /** Loads the given URL in the WebView. */
    fun loadUrl(url: String) {
        awContents.loadUrl(url)
    }

    /** Loads the given URL with additional HTTP headers. */
    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        awContents.loadUrl(url, additionalHttpHeaders)
    }

    /**
     * Evaluates JavaScript in the context of the currently displayed page.
     *
     * @param script The JavaScript code to evaluate
     * @param resultCallback Callback receiving the result of the evaluation as a JSON string
     */
    fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        awContents.evaluateJavaScript(script) { result ->
            resultCallback?.onReceiveValue(result)
        }
    }

    /**
     * Injects a Java object into the JavaScript context of the WebView.
     *
     * Methods annotated with [android.webkit.JavascriptInterface] on the object
     * become callable from JavaScript using `window.<name>.<method>()`.
     *
     * @param obj The Java object to inject
     * @param name The name for the object in JavaScript
     */
    fun addJavascriptInterface(obj: Any, name: String) {
        awContents.addJavascriptInterface(obj, name)
    }

    /**
     * Removes a previously injected JavaScript interface.
     *
     * @param name The name of the interface to remove
     */
    fun removeJavascriptInterface(name: String) {
        awContents.removeJavascriptInterface(name)
    }

    /** Returns whether the WebView has a back history item. */
    fun canGoBack(): Boolean = awContents.canGoBack()

    /** Returns whether the WebView has a forward history item. */
    fun canGoForward(): Boolean = awContents.canGoForward()

    /** Goes back in the WebView history. */
    fun goBack() {
        awContents.goBack()
    }

    /** Goes forward in the WebView history. */
    fun goForward() {
        awContents.goForward()
    }

    /** Reloads the current URL. */
    fun reload() {
        awContents.reload()
    }

    /** Stops the current load. */
    fun stopLoading() {
        awContents.stopLoading()
    }

    /** Clears the WebView cache. */
    fun clearCache(includeDiskFiles: Boolean) {
        awContents.clearCache(includeDiskFiles)
    }

    /** Clears the WebView's internal back/forward list. */
    fun clearHistory() {
        awContents.clearHistory()
    }

    /** Returns the URL currently being displayed. */
    val url: String?
        get() = awContents.url?.toString()

    /** Returns the title of the current page. */
    fun getTitle(): String? = awContents.title

    /**
     * Sets the initial scale for the WebView content by adjusting the text zoom level.
     *
     * @param scaleInPercent The initial scale in percent, 0 means default (100%)
     */
    fun setInitialScale(scaleInPercent: Int) {
        if (scaleInPercent > 0) {
            awSettings.textZoom = scaleInPercent
        }
    }

    /**
     * Registers a callback to be invoked when the host application needs to handle a download.
     *
     * @param listener A callback matching the Android WebView DownloadListener interface
     */
    fun setDownloadListener(
        listener: (url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) -> Unit,
    ) {
        contentsClient.downloadListener = listener
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        try {
            containerView.setBackgroundColor(color)
        } catch (e: Exception) {
            Timber.w(e, "Failed to set background color on container view")
        }
    }

    /**
     * Cleans up the underlying Chromium engine resources.
     * Must be called when the view is no longer needed to prevent memory leaks.
     */
    fun destroy() {
        try {
            awContents.destroy()
        } catch (e: Exception) {
            Timber.w(e, "Error destroying AwContents")
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.d("BundledWebView detached from window")
    }
}
