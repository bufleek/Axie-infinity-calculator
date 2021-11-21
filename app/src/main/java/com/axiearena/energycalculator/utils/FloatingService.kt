package com.axiearena.energycalculator.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.ui.floating_windows.FloatingWindow
import com.axiearena.energycalculator.ui.main.MainActivity
import com.axiearena.energycalculator.ui.settings.SettingsActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FloatingService : Service(), ServiceActions.OnServiceAction {
    private val TAG = "FloatingService"

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopService() {
        FloatingWindowActions.getInstance().listener = null
        ServiceActions.getInstance().listener = null
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
        ServiceActions.getInstance().listener = this
        Log.d(TAG, "onStartCommand: started")
        val command = intent?.getStringExtra(INTENT_COMMAND)
        val themeColor =
            intent?.getIntExtra(INTENT_THEME_COLOR, ContextCompat.getColor(this, R.color.overlay))
                ?: ContextCompat.getColor(this, R.color.overlay)
        val isBasicMode =
            intent?.getBooleanExtra(INTENT_IS_BASIC_MODE, false) ?: false

        val session: Session? = Gson().fromJson(intent?.getStringExtra(INTENT_SESSION), object : TypeToken<Session>(){}.type)

        if (command == INTENT_COMMAND_EXIT) {
            stopService()
            return START_NOT_STICKY
        }

        showNotification()

        if (!drawOverOtherAppsEnabled()) {
            startMainActivity()
        }

        if(FloatingWindowActions.getInstance().listener == null){
            FloatingWindow(this, themeColor, isPcMode = false, isBasicMode = isBasicMode, session = session)
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        WindowActions.getInstance().listener?.onConfigsChange()
    }

    companion object {
        const val INTENT_COMMAND = "com.axiecalc"
        const val INTENT_COMMAND_EXIT = "EXIT"
        const val INTENT_COMMAND_FLOAT = "FLOAT"
        const val INTENT_THEME_COLOR = "FLOAT"
        const val INTENT_IS_BASIC_MODE = "ISBASIC"
        const val INTENT_SESSION = "SESSION"

        private const val NOTIFICATION_CHANNEL_GENERAL = "axie_general"
        private const val CODE_FOREGROUND_SERVICE = 1
        private const val CODE_EXIT_INTENT = 2
    }

    override fun onBackUpSession(session: Session) {
        val sessionPref = stringPreferencesKey("session")
        CoroutineScope(Dispatchers.Main).launch {
            backupDataStore.edit {
                it[sessionPref] = Gson().toJson(session)
            }
        }
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