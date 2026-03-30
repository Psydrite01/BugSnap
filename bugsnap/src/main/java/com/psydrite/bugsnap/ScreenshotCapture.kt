package com.psydrite.bugsnap

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.core.graphics.createBitmap

object ScreenshotCapture {

    fun capture(activity: Activity, onCaptured: (Bitmap?) -> Unit) {
        captureWithPixelCopy(activity.window, onCaptured)
    }

    // API 26+ — accurate, captures everything including surfaces
    private fun captureWithPixelCopy(window: Window, onCaptured: (Bitmap?) -> Unit) {
        val view = window.decorView
        val bitmap = createBitmap(view.width, view.height)

        PixelCopy.request(
            window,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    onCaptured(bitmap)
                } else {
                    onCaptured(null)
                }
            },
            Handler(Looper.getMainLooper())
        )
    }
}