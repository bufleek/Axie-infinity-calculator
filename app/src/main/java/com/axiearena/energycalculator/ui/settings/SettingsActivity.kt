package com.axiearena.energycalculator.ui.settings
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerView

class SettingsActivity : AppCompatActivity() {
    private val dataStore by lazy { (application as AxieCalculator).dataStore }
    private val themeColorPref by lazy { intPreferencesKey("color")  }
    private val soundPref by lazy { booleanPreferencesKey("sound")  }
    private var themeColor: Int? = null
    private lateinit var colorPicker: ColorPickerView
    private lateinit var switchSound: SwitchCompat
    private lateinit var btnResetColor: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(ContextCompat.getColor(this, R.color.surface))
        setContentView(R.layout.activity_settings)
        colorPicker = findViewById(R.id.colorPicker)
        switchSound = findViewById(R.id.switch_sound)
        btnResetColor = findViewById(R.id.btn_reset_color)

        val colorFlow: Flow<Int> = dataStore.data.map {
            it[themeColorPref] ?: ContextCompat.getColor(this, R.color.overlay)
        }
        val soundFlow: Flow<Boolean> = dataStore.data.map {
            it[soundPref] ?: true
        }

        CoroutineScope(Dispatchers.Main).launch {
            colorFlow.collectLatest {
                colorPicker.setInitialColor(it)
                colorPicker.visibility = View.VISIBLE
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            soundFlow.collectLatest {
                switchSound.isChecked = it
            }
        }

        colorPicker.subscribe { color, fromUser, shouldPropagate ->
            FloatingWindowActions.getInstance().listener?.onColorChanged(color)
            CardCounterActions.getInstance().listener?.onColorChanged(color)
            SlpActions.getInstance().listener?.onColorChanged(color)
            PvpActions.getInstance().listener?.onColorChanged(color)
            themeColor = color
        }

        btnResetColor.setOnClickListener {
            colorPicker.setInitialColor(ContextCompat.getColor(this, R.color.overlay))
        }

        switchSound.setOnCheckedChangeListener { button, state ->
            FloatingWindowActions.getInstance().listener?.onSoundConfigChanged(state)
            CoroutineScope(Dispatchers.Main).launch {
                dataStore.edit {
                    it[soundPref] = state
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        FloatingWindowActions.getInstance().listener = null
        CoroutineScope(Dispatchers.Main).launch {
            themeColor?.let { theme ->
                dataStore.edit {
                    it[themeColorPref] = theme
                }
            }
        }
        finish()
    }
}