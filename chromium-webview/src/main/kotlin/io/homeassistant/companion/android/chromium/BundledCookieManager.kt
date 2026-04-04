package io.homeassistant.companion.android.chromium

import org.chromium.android_webview.AwCookieManager
import timber.log.Timber

/**
 * Cookie manager for the bundled Chromium WebView engine.
 *
 * Wraps Chromium's [AwCookieManager] to provide an API compatible with
 * [android.webkit.CookieManager] for the cookie operations used by the app.
 *
 * This singleton must be used instead of [android.webkit.CookieManager.getInstance]
 * when using the bundled WebView, since the system CookieManager only manages
 * cookies for the system WebView provider.
 */
object BundledCookieManager {

    private val cookieManager: AwCookieManager by lazy {
        AwCookieManager()
    }

    /**
     * Sets whether the WebView should accept cookies.
     *
     * @param accept Whether to accept cookies
     */
    fun setAcceptCookie(accept: Boolean) {
        cookieManager.setAcceptCookie(accept)
    }

    /**
     * Gets the cookie value for the given URL.
     *
     * @param url The URL for which the cookies are requested
     * @return A string containing all cookies for the URL, or null if there are no cookies
     */
    fun getCookie(url: String): String? {
        return try {
            cookieManager.getCookie(url)
        } catch (e: Exception) {
            Timber.w(e, "Failed to get cookie for url")
            null
        }
    }

    /**
     * Sets a cookie for the given URL.
     *
     * @param url The URL for which the cookie is to be set
     * @param value The cookie value as a string in the format of "Set-Cookie" response header
     */
    fun setCookie(url: String, value: String) {
        try {
            cookieManager.setCookie(url, value)
        } catch (e: Exception) {
            Timber.w(e, "Failed to set cookie for url")
        }
    }

    /**
     * Removes all session cookies.
     */
    fun removeSessionCookies() {
        cookieManager.removeSessionCookies()
    }

    /**
     * Removes all cookies.
     */
    fun removeAllCookies() {
        cookieManager.removeAllCookies()
    }

    /**
     * Ensures all cookies currently accessible through the getCookie API are written to
     * persistent storage.
     */
    fun flush() {
        cookieManager.flushCookieStore()
    }
}
