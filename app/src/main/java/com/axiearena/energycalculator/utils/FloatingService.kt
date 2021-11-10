package com.axiearena.energycalculator.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.ui.floating_windows.FloatingWindow
import com.axiearena.energycalculator.ui.main.MainActivity
import com.axiearena.energycalculator.ui.settings.SettingsActivity

class FloatingService : Service() {
    private val TAG = "FloatingService"

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopService() {
        FloatingWindowActions.getInstance().listener = null
        stopForeground(true)
        stopSelf()
    }

    private fun showNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val exitIntent = Intent(this, FloatingService::class.java).apply {
            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
        }

        val exitPendingIntent = PendingIntent.getService(
            this, CODE_EXIT_INTENT, exitIntent, 0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        getString(R.string.notification_channel_general),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (ignored: Exception) {
            }
        }
        with(
            NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_GENERAL
            )
        ) {
            setTicker(null)
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.notification_text))
            setAutoCancel(false)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.mipmap.ic_launcher)
            priority = Notification.PRIORITY_DEFAULT
            setContentIntent(exitPendingIntent)

            startForeground(CODE_FOREGROUND_SERVICE, build())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: started")
        val command = intent?.getStringExtra(INTENT_COMMAND)
        val themeColor =
            intent?.getIntExtra(INTENT_COMMAND, ContextCompat.getColor(this, R.color.overlay))
                ?: ContextCompat.getColor(this, R.color.overlay)

        if (command == INTENT_COMMAND_EXIT) {
            stopService()
            return START_NOT_STICKY
        }

        showNotification()

        if (!drawOverOtherAppsEnabled()) {
            startMainActivity()
        }

        if(FloatingWindowActions.getInstance().listener == null){
            FloatingWindow(this, themeColor)
        }
        return START_STICKY
    }

    companion object {
        const val INTENT_COMMAND = "com.axiecalc"
        const val INTENT_COMMAND_EXIT = "EXIT"
        const val INTENT_COMMAND_FLOAT = "FLOAT"
        const val INTENT_THEME_COLOR = "FLOAT"

        private const val NOTIFICATION_CHANNEL_GENERAL = "axie_general"
        private const val CODE_FOREGROUND_SERVICE = 1
        private const val CODE_EXIT_INTENT = 2
    }
}

fun Context.drawOverOtherAppsEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        true
    } else {
        Settings.canDrawOverlays(this)
    }
}


fun Context.startMainActivity(message: String? = null) {
    startActivity(
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (message != null){
                putExtra(MainActivity.INTENT_ROUNDS_EXHAUSTED, message)
            }
        }
    )
}


fun Context.startSettingsActivity() {
    startActivity(
        Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}