package io.homeassistant.companion.android.chromium

import android.app.Application
import org.chromium.android_webview.AwBrowserProcess
import org.chromium.android_webview.shell.AwShellResourceProvider
import org.chromium.base.CommandLine
import org.chromium.base.ContextUtils
import timber.log.Timber

/**
 * Handles one-time initialization of the bundled Chromium browser engine.
 *
 * Must be called before creating any [BundledWebView] instance, typically in
 * [Application.onCreate]. Calling [initialize] multiple times is safe; subsequent
 * calls are no-ops.
 */
object ChromiumInitializer {

    @Volatile
    private var initialized = false

    /**
     * Initializes the bundled Chromium browser process.
     *
     * This loads the native Chromium library, registers resources, and starts the
     * browser process. Must be called on the main thread before any [BundledWebView]
     * is created.
     *
     * @param application The application context
     */
    fun initialize(application: Application) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            try {
                AwShellResourceProvider.registerResources(application)
                CommandLine.init(arrayOf(""))
                ContextUtils.initApplicationContext(application)
                AwBrowserProcess.loadLibrary("chromium_webview")
                AwBrowserProcess.start()
                initialized = true
                Timber.i("Bundled Chromium browser process initialized")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize bundled Chromium browser process")
                throw e
            }
        }
    }

    /**
     * Returns whether the Chromium browser process has been initialized.
     */
    fun isInitialized(): Boolean = initialized
}
