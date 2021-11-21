package com.axiearena.energycalculator.ui.main

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.ui.floating_windows.Arena
import com.axiearena.energycalculator.ui.floating_windows.CardCounter
import com.axiearena.energycalculator.ui.floating_windows.FloatingWindow
import com.axiearena.energycalculator.ui.floating_windows.SLPCalculator
import com.axiearena.energycalculator.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PcActivity : AppCompatActivity() {
    private var themeColor: Int? = null
    private val dataStore by lazy { (application as AxieCalculator).dataStore }

    private lateinit var quarter1: FrameLayout
    private lateinit var quarter2: FrameLayout
    private lateinit var quarter3: FrameLayout
    private lateinit var quarter4: FrameLayout

    private var session: Session? = null

    private val floatingWindow by lazy {
        FloatingWindow(
            baseContext,
            themeColor!!,
            isPcMode = true,
            session = session
        )
    }
    private val cardCounter by lazy { CardCounter(baseContext, isPcMode = true, session = session) }
    private val slpCalculator by lazy {
        SLPCalculator(
            baseContext,
            themeColor!!,
            isPcMode = true,
            session = session
        )
    }
    private val arena by lazy { Arena(baseContext, isPcMode = true, session = session) }

    override fun onResume() {
        super.onResume()
        FloatingWindowActions.getInstance().listener = floatingWindow
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc)
        setStatusBarColor(ContextCompat.getColor(this, R.color.surface))
        themeColor = ContextCompat.getColor(this, R.color.surface)

        session = Gson().fromJson(
            intent?.getStringExtra(INTENT_SESSION),
            object : TypeToken<Session>() {}.type
        )

        quarter1 = findViewById(R.id.frame_pc_quarter_1)
        quarter2 = findViewById(R.id.frame_pc_quarter_2)
        quarter3 = findViewById(R.id.frame_pc_quarter_3)
        quarter4 = findViewById(R.id.frame_pc_quarter_4)

        quarter1.removeAllViews()
        quarter2.removeAllViews()
        quarter3.removeAllViews()
        quarter4.removeAllViews()

        quarter1.addView(floatingWindow.root)
        quarter2.addView(cardCounter.root)
        quarter3.addView(arena.root)
        quarter4.addView(slpCalculator.root)

        val colorPrefKey = intPreferencesKey("color")
        val colorFlow: Flow<Int> = dataStore.data.map {
            it[colorPrefKey] ?: ContextCompat.getColor(this@PcActivity, R.color.overlay)
        }

        CoroutineScope(Dispatchers.Main).launch {
            colorFlow.collectLatest {
                themeColor = it
                FloatingWindowActions.getInstance().listener?.onColorChanged(it)
                CardCounterActions.getInstance().listener?.onColorChanged(it)
                SlpActions.getInstance().listener?.onColorChanged(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        FloatingWindowActions.getInstance().listener?.onClose()
        CardCounterActions.getInstance().listener?.onClose()
        ArenaActions.getInstance().listener?.onClose()
        SlpActions.getInstance().listener?.onClose()

        val session = Session(
            System.currentTimeMillis(),
            isPcMode = true,
            isBasicMode = false,
            FloatingWindowActions.getInstance().energyCalcData,
            CardCounterActions.getInstance().cardCounterData,
            ArenaActions.getInstance().arenaData,
            SlpActions.getInstance().slpCalculatorData
        )

        val sessionPref = stringPreferencesKey("session")
        CoroutineScope(Dispatchers.Main).launch {
            backupDataStore.edit {
                it[sessionPref] = Gson().toJson(session)
            }
        }
        FloatingWindowActions.getInstance().listener = null
    }

    companion object {
        const val INTENT_SESSION = "SESSION"
    }
}