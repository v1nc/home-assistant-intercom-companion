package io.homeassistant.companion.android.chromium

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Rect
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import org.chromium.android_webview.AwContents

/**
 * A container view for [AwContents] that uses software rendering via [AwContents.onDraw].
 *
 * Unlike [org.chromium.android_webview.test.AwTestContainerView] which relies on hardware
 * draw functors that may not work on all devices, this view directly calls [AwContents.onDraw]
 * with a software [Canvas], ensuring content renders on any Android 10+ device.
 */
internal class SoftwareDrawContainerView(context: Context) : FrameLayout(context) {

    private var awContents: AwContents? = null

    val internalAccessDelegate = object : AwContents.InternalAccessDelegate {
        override fun overScrollBy(
            deltaX: Int,
            deltaY: Int,
            scrollX: Int,
            scrollY: Int,
            scrollRangeX: Int,
            scrollRangeY: Int,
            maxOverScrollX: Int,
            maxOverScrollY: Int,
            isTouchEvent: Boolean,
        ) {
            this@SoftwareDrawContainerView.overScrollBy(
                deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent,
            )
        }

        override fun super_scrollTo(scrollX: Int, scrollY: Int) {
            this@SoftwareDrawContainerView.scrollTo(scrollX, scrollY)
        }

        override fun setMeasuredDimension(measuredWidth: Int, measuredHeight: Int) {
            this@SoftwareDrawContainerView.setMeasuredDimension(measuredWidth, measuredHeight)
        }

        override fun super_getScrollBarStyle(): Int = this@SoftwareDrawContainerView.scrollBarStyle

        override fun super_startActivityForResult(intent: android.content.Intent?, requestCode: Int) {}

        override fun super_onConfigurationChanged(newConfig: Configuration?) {}

        override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {}

        override fun super_onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
            event?.let { this@SoftwareDrawContainerView.onKeyUp(keyCode, it) } ?: false

        override fun super_dispatchKeyEvent(event: KeyEvent?): Boolean =
            event?.let { super@SoftwareDrawContainerView.dispatchKeyEvent(it) } ?: false

        override fun super_onGenericMotionEvent(event: MotionEvent?): Boolean =
            event?.let { super@SoftwareDrawContainerView.onGenericMotionEvent(it) } ?: false
    }

    val nativeDrawFunctorFactory: AwContents.NativeDrawFunctorFactory? = null

    fun initialize(contents: AwContents) {
        awContents = contents
        // Use software rendering for maximum compatibility
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onDraw(canvas: Canvas) {
        awContents?.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        awContents?.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        awContents?.onSizeChanged(w, h, ow, oh)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        awContents?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        awContents?.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        awContents?.onVisibilityChanged(changedView, visibility)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        awContents?.onWindowVisibilityChanged(visibility)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        awContents?.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return awContents?.onTouchEvent(event) ?: super.onTouchEvent(event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        return awContents?.onCreateInputConnection(outAttrs) ?: super.onCreateInputConnection(outAttrs)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return awContents?.onKeyUp(keyCode, event) ?: super.onKeyUp(keyCode, event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return awContents?.dispatchKeyEvent(event) ?: super.dispatchKeyEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        awContents?.onConfigurationChanged(newConfig)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun computeScroll() {
        awContents?.computeScroll()
    }
}
