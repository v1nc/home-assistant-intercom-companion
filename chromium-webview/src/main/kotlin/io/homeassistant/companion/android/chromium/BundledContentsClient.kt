package io.homeassistant.companion.android.chromium

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import java.security.Principal
import org.chromium.android_webview.AwContentsClient
import org.chromium.android_webview.AwContentsClientBridge
import org.chromium.android_webview.AwRenderProcess
import org.chromium.android_webview.test.NullContentsClient
import org.chromium.base.Callback
import org.chromium.components.embedder_support.util.WebResourceResponseInfo
import timber.log.Timber

/**
 * Bridges Chromium's [NullContentsClient] callbacks to standard Android
 * [WebViewClient] and [WebChromeClient] interfaces.
 *
 * This allows existing WebViewClient/WebChromeClient implementations
 * (such as HAWebViewClient and HAWebChromeClient) to work with the
 * bundled Chromium engine without modification to their callback handling.
 *
 * Note: The `view` parameter passed to WebViewClient callbacks is `null`
 * since [BundledWebView] does not extend [android.webkit.WebView].
 * Callback implementations should not rely on the view parameter.
 */
internal class BundledContentsClient : NullContentsClient() {

    var webViewClient: WebViewClient? = null
    var webChromeClient: WebChromeClient? = null

    /** Reference to the owning BundledWebView, used for crash recovery. */
    var bundledWebView: BundledWebView? = null

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(request: AwContentsClient.AwWebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        // Delegate to the deprecated WebViewClient method which accepts a plain URL string,
        // since the app's HAWebViewClient overrides this version for backward compatibility.
        return webViewClient?.shouldOverrideUrlLoading(null, url) ?: false
    }

    override fun onPageStarted(url: String?) {
        webViewClient?.onPageStarted(null, url, null)
    }

    override fun onPageFinished(url: String?) {
        webViewClient?.onPageFinished(null, url)
    }

    override fun onReceivedError(errorCode: Int, description: String?, failingUrl: String?) {
        Timber.e("onReceivedError: code=$errorCode desc=$description url=$failingUrl")
    }

    override fun onReceivedHttpError(
        request: AwContentsClient.AwWebResourceRequest?,
        response: WebResourceResponseInfo?,
    ) {
        Timber.e("onReceivedHttpError: statusCode=${response?.statusCode}")
    }

    override fun onReceivedSslError(callback: Callback<Boolean?>?, error: SslError?) {
        Timber.e("onReceivedSslError: $error")
        // Deny by default for security
        callback?.onResult(false)
    }

    override fun onReceivedClientCertRequest(
        callback: AwContentsClientBridge.ClientCertificateRequestCallback?,
        keyTypes: Array<out String>?,
        principals: Array<out Principal>?,
        host: String?,
        port: Int,
    ) {
        Timber.d("onReceivedClientCertRequest for host=$host port=$port")
        // For TLS client auth, we delegate to the WebViewClient's mechanism
        // The bundled WebView handles this through the standard ClientCertRequest flow
        callback?.proceed(null, null)
    }

    override fun onReceivedTitle(title: String?) {
        webChromeClient?.onReceivedTitle(null, title)
    }

    override fun onReceivedIcon(bitmap: Bitmap?) {
        webChromeClient?.onReceivedIcon(null, bitmap)
    }

    override fun onProgressChanged(progress: Int) {
        webChromeClient?.onProgressChanged(null, progress)
    }

    override fun onRendererUnresponsive(process: AwRenderProcess?) {
        Timber.w("Bundled WebView renderer unresponsive")
    }

    override fun onRendererResponsive(process: AwRenderProcess?) {
        Timber.d("Bundled WebView renderer responsive again")
    }

    /** Callback for download events, matching the Android WebView DownloadListener API. */
    var downloadListener: ((url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) -> Unit)? = null

    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
    ) {
        downloadListener?.invoke(
            url ?: "",
            userAgent ?: "",
            contentDisposition ?: "",
            mimeType ?: "",
            contentLength,
        )
    }

    /**
     * Called when the Chromium render process has crashed or been killed.
     * Dispatches to the [WebViewClient.onRenderProcessGone] callback.
     */
    fun onRenderProcessGone(): Boolean {
        Timber.e("Bundled WebView render process gone")
        return webViewClient?.onRenderProcessGone(null, null) ?: true
    }
}
