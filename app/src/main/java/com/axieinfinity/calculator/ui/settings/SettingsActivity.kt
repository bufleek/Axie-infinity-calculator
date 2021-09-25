package com.axieinfinity.calculator.ui.settings
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.axieinfinity.calculator.R
import com.axieinfinity.calculator.utils.AxieCalculator
import com.axieinfinity.calculator.utils.FloatingWindowActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerView

class SettingsActivity : AppCompatActivity() {
    private val dataStore by lazy { (application as AxieCalculator).dataStore }
    private val themeColorPref by lazy { intPreferencesKey("color") }
    private var themeColor: Int? = null
    private lateinit var colorPicker: ColorPickerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        colorPicker = findViewById(R.id.colorPicker)

        val colorFlow: Flow<Int> = dataStore.data.map {
            it[themeColorPref] ?: ContextCompat.getColor(this, R.color.overlay)
        }

        CoroutineScope(Dispatchers.Main).launch {
            colorFlow.collectLatest {
                colorPicker.setInitialColor(it)
                colorPicker.visibility = View.VISIBLE
            }
        }

        colorPicker.subscribe { color, fromUser, shouldPropagate ->
            FloatingWindowActions.getInstance().listener?.onColorChanged(color)
            themeColor = color
        }
    }

    override fun onPause() {
        super.onPause()
        FloatingWindowActions.getInstance().listener = null
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