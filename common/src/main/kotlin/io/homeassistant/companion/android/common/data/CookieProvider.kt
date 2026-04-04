package io.homeassistant.companion.android.common.data

/**
 * Abstraction over WebView cookie storage to decouple the networking layer
 * from a specific WebView implementation.
 *
 * The concrete implementation is provided by the app module and bridges to
 * the bundled Chromium cookie manager.
 */
interface CookieProvider {

    /**
     * Gets the cookie value for the given URL.
     *
     * @param url The URL for which cookies are requested
     * @return A string containing all cookies separated by "; ", or null
     */
    fun getCookie(url: String): String?

    /**
     * Sets a cookie for the given URL.
     *
     * @param url The URL for which the cookie is to be set
     * @param value The cookie string in "Set-Cookie" header format
     */
    fun setCookie(url: String, value: String)

    companion object {
        /**
         * The global cookie provider instance. Must be set before any networking calls.
         */
        @Volatile
        var instance: CookieProvider? = null
    }
}
