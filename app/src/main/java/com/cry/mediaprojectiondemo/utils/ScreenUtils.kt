package com.cry.mediaprojectiondemo.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtils {
    private val metrics = DisplayMetrics()

    private lateinit var windowManager: WindowManager
    private var isInitialized = false

    fun metrics(context: Context): DisplayMetrics {
        if (isInitialized && metrics.widthPixels != 0 && metrics.heightPixels != 0) {
            return metrics
        }

        if (!::windowManager.isInitialized) {
            windowManager =
                context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            isInitialized = true
        }
        windowManager.defaultDisplay?.getRealMetrics(metrics)
        return metrics
    }
}