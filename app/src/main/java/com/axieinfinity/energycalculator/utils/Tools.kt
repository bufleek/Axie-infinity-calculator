package com.axieinfinity.energycalculator

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.axieinfinity.energycalculator.utils.FloatingService
import com.axieinfinity.energycalculator.utils.FloatingService.Companion.INTENT_COMMAND

fun Context.startFloatingService(command: String = "", themeColor: Int) {
    val intent = Intent(this, FloatingService::class.java)
    intent.putExtra(FloatingService.INTENT_THEME_COLOR, themeColor)
    if (command.isNotBlank()) intent.putExtra(INTENT_COMMAND, command)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ContextCompat.startForegroundService(this, intent)
    } else {
        this.startService(intent)
    }
}

fun getCurrentDisplayMetrics(windowManager: WindowManager): DisplayMetrics {
    val dm = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(dm)
    return dm
}

val windowParams = WindowManager.LayoutParams(
    0,
    0,
    0,
    0,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    },
    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
    PixelFormat.TRANSLUCENT
)