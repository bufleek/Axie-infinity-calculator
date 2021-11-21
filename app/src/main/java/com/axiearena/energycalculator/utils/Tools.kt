package com.axiearena.energycalculator.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.utils.FloatingService.Companion.INTENT_COMMAND
import com.google.gson.Gson

fun Context.startFloatingService(
    command: String = "",
    themeColor: Int,
    isBasicMode: Boolean = false,
    session: Session? = null
) {
    val intent = Intent(this, FloatingService::class.java)
    intent.putExtra(FloatingService.INTENT_THEME_COLOR, themeColor)
    intent.putExtra(FloatingService.INTENT_IS_BASIC_MODE, isBasicMode)
    intent.putExtra(FloatingService.INTENT_SESSION, Gson().toJson(session))

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

fun Activity.setStatusBarColor(color: Int) {
    var flags = window?.decorView?.systemUiVisibility
    if (flags != null) {
        if (isColorDark(color)) {
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.decorView.systemUiVisibility = flags
        } else {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.systemUiVisibility = flags
        }
    }
    window.statusBarColor = color
}

fun isColorDark(color: Int): Boolean {
    val darkness =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
    return darkness >= 0.5
}

fun Context.playSound() {
    MediaPlayer.create(this, resources.getIdentifier("click", "raw", packageName)).start()
}