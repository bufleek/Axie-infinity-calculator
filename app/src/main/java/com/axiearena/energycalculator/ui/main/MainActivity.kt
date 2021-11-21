package com.axiearena.energycalculator.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.ui.settings.SettingsActivity
import com.axiearena.energycalculator.utils.*
import com.axiearena.energycalculator.utils.FloatingService.Companion.INTENT_COMMAND_FLOAT
import com.google.android.material.card.MaterialCardView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

class MainActivity : AppCompatActivity() {
    private var themeColor: Int? = null
    private val dataStore by lazy { (application as AxieCalculator).dataStore }
    private lateinit var modeBasic: MaterialCardView
    private lateinit var modeNormal: MaterialCardView
    private lateinit var modePc: MaterialCardView
    private lateinit var cusomization: MaterialCardView
    private var isBasicMode = false
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(ContextCompat.getColor(this, R.color.primary))
        setContentView(R.layout.activity_main)
        FirebaseAnalytics.getInstance(this)

        modeBasic = findViewById(R.id.mode_basic)
        modeNormal = findViewById(R.id.mode_normal)
        modePc = findViewById(R.id.mode_pc)
        cusomization = findViewById(R.id.customization)

        modeBasic.setOnClickListener {
            isBasicMode = true
            showFloatingWindow(true)
        }

        modeNormal.setOnClickListener { showFloatingWindow() }

        modePc.setOnClickListener {
            startActivity(Intent(this, PcActivity::class.java))
            finish()
        }

        cusomization.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        if (FloatingWindowActions.getInstance().listener == null) {
            val sessionPreference = stringPreferencesKey("session")
            val sessionFlow: Flow<String?> = backupDataStore
                .data.map {
                    it[sessionPreference]
                }
            CoroutineScope(Dispatchers.Main).launch {
                sessionFlow.collect {
                    session = Gson().fromJson(it, object : TypeToken<Session>() {}.type)
                    if (it != null) {
                        delay(500)
                        SessionFragment.newInstance(it).show(supportFragmentManager, null)
                    }
                    cancel()
                }
            }
        }
    }

    fun showFloatingWindow(isBasicMode: Boolean = false, session: Session? = null) {
        if (!drawOverOtherAppsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission()
            }
        } else {
            startFloatingService(
                themeColor = themeColor ?: ContextCompat.getColor(
                    this@MainActivity,
                    R.color.overlay
                ),
                isBasicMode = isBasicMode,
                session = session
            )

            val colorPrefKey = intPreferencesKey("color")
            val colorFlow: Flow<Int> = dataStore.data.map {
                it[colorPrefKey] ?: ContextCompat.getColor(this@MainActivity, R.color.overlay)
            }

            CoroutineScope(Dispatchers.Main).launch {
                colorFlow.collectLatest {
                    themeColor = it
                    FloatingWindowActions.getInstance().listener?.onColorChanged(it)
                    CardCounterActions.getInstance().listener?.onColorChanged(it)
                    SlpActions.getInstance().listener?.onColorChanged(it)
                }
            }

            val soundPrefKey = booleanPreferencesKey("sound")
            val soundFlow: Flow<Boolean> = dataStore.data.map {
                it[soundPrefKey] ?: true
            }
            CoroutineScope(Dispatchers.Main).launch {
                soundFlow.collectLatest {
                    FloatingWindowActions.getInstance().listener?.onSoundConfigChanged(it)
                }
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (drawOverOtherAppsEnabled()) {
                startFloatingService(
                    INTENT_COMMAND_FLOAT,
                    themeColor ?: ContextCompat.getColor(this@MainActivity, R.color.overlay),
                    isBasicMode = isBasicMode,
                    session = session
                )
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted. The app will not function properly",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        finish()
    }

    private fun showDialog(titleText: String, messageText: String) {
        with(AlertDialog.Builder(this)) {
            title = titleText
            setMessage(messageText)
            setPositiveButton(R.string.common_ok) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        with(AlertDialog.Builder(this)) {
            title = "Permission required"
            setMessage("Please grant permission to draw over other apps")
            setPositiveButton("Allow") { dialog, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                try {
                    startActivityForResult(intent, PERMISSION_REQUEST_CODE)
                } catch (e: Exception) {
                    showDialog(
                        getString(R.string.permission_error_title),
                        getString(R.string.permission_error_text)
                    )
                }
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 0
        const val INTENT_ROUNDS_EXHAUSTED = "ROUNDS_EXHAUSTED"
        const val MESSAGE_ROUNDS_EXHAUSTED =
            "Your free rounds have been exhausted, earn more rewards or unlock our paid plan"
    }
}