package com.axieinfinity.energycalculator.ui.main

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
import androidx.datastore.preferences.core.intPreferencesKey
import com.axieinfinity.energycalculator.R
import com.axieinfinity.energycalculator.startFloatingService
import com.axieinfinity.energycalculator.utils.AxieCalculator
import com.axieinfinity.energycalculator.utils.CardCounterActions
import com.axieinfinity.energycalculator.utils.FloatingService.Companion.INTENT_COMMAND_FLOAT
import com.axieinfinity.energycalculator.utils.FloatingWindowActions
import com.axieinfinity.energycalculator.utils.drawOverOtherAppsEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var themeColor: Int? = null
    private val dataStore by lazy { (application as AxieCalculator).dataStore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showFloatingWindow()
    }

    private fun showFloatingWindow() {
        if ((application as AxieCalculator).rewardsEarned >= REWARDS_REQUIRED) {
            if (!drawOverOtherAppsEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission()
                }
            } else {
                startFloatingService(
                    themeColor = themeColor ?: ContextCompat.getColor(
                        this@MainActivity,
                        R.color.overlay
                    )
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
                    }
                }
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (drawOverOtherAppsEnabled()) {
                startFloatingService(
                    INTENT_COMMAND_FLOAT,
                    themeColor ?: ContextCompat.getColor(this@MainActivity, R.color.overlay)
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
        const val REWARDS_REQUIRED = 0
        const val INTENT_ROUNDS_EXHAUSTED = "ROUNDS_EXHAUSTED"
        const val MESSAGE_ROUNDS_EXHAUSTED =
            "Your free rounds have been exhausted, earn more rewards or unlock our paid plan"
    }
}