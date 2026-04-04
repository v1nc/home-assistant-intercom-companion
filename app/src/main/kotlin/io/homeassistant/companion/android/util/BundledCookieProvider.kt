package io.homeassistant.companion.android.util

import io.homeassistant.companion.android.chromium.BundledCookieManager
import io.homeassistant.companion.android.common.data.CookieProvider

/**
 * [CookieProvider] implementation backed by the bundled Chromium cookie manager.
 *
 * Registered as [CookieProvider.instance] during application initialization to
 * allow the networking layer in `:common` to access cookies from the bundled
 * WebView without depending on the `:chromium-webview` module.
 */
object BundledCookieProvider : CookieProvider {

    override fun getCookie(url: String): String? = BundledCookieManager.getCookie(url)

    override fun setCookie(url: String, value: String) = BundledCookieManager.setCookie(url, value)
}
