package com.axieinfinity.calculator

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.axieinfinity.calculator.utils.FloatingService
import com.axieinfinity.calculator.utils.FloatingService.Companion.INTENT_COMMAND

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