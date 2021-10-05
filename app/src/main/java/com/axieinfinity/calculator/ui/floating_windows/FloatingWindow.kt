package com.axieinfinity.calculator.ui.floating_windows

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.axieinfinity.calculator.R
import com.axieinfinity.calculator.data.models.EnergyHistory
import com.axieinfinity.calculator.getCurrentDisplayMetrics
import com.axieinfinity.calculator.ui.main.MainActivity
import com.axieinfinity.calculator.utils.*
import com.axieinfinity.calculator.windowParams

class FloatingWindow(
    private val context: Context,
    private val color: Int,
    private val isSubscribed: Boolean = true
) : FloatingWindowActions.OnFloatingWindowAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val root = layoutInflater.inflate(R.layout.item_main_popup, null)
    private val tvEnergyUsed: TextView = root.findViewById(R.id.energy_used)
    private val tvEnergyDestroyed: TextView = root.findViewById(R.id.energy_destroyed)
    private val tvEnergyGained: TextView = root.findViewById(R.id.energy_gained)
    private val content: View = root.findViewById(R.id.content)
    private val tvCurrentEnergy: TextView = root.findViewById(R.id.current_energy)
    private val tvCurrentRound: TextView = root.findViewById(R.id.current_round)
    private val reset = root.findViewById<TextView>(R.id.reset_rounds)
    private val next = root.findViewById<TextView>(R.id.calculate)
    private val undo = root.findViewById<ImageView>(R.id.img_undo)
    private var energyUsed = 0
    private var energyDestroyed = 0
    private var energyGained = 0
    private var currentEnergy = 3
    private var currentRound = 1
    private var remainingRounds = MAX_ROUNDS
    private var history: ArrayList<EnergyHistory> = ArrayList()
    private var isOpen = false

    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics(windowManager)
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = (widthInDp * dm.density).toInt()
        params.height = (heightInDp * dm.density).toInt()
        params.x = 0
//            ((dm.widthPixels - params.width) - (dm.density * 10)).toInt()
        params.y = 0
//            if ((dm.density * 100) < (dm.density * Arena.ARENA_HEIGHT)) ((dm.density * Arena.ARENA_HEIGHT + 20).toInt()) else params.y
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, windowInputWidth, windowInputHeight)
    }

    private fun initWindow() {
        root.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        root.findViewById<ImageView>(R.id.increase_energy_used).setOnClickListener {
            energyUsed++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_used).setOnClickListener {
            if (energyUsed != 0) {
                energyUsed--
                updateViews()
            }
        }

        root.findViewById<ImageView>(R.id.increase_energy_destroyed).setOnClickListener {
            energyDestroyed++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_destroyed).setOnClickListener {
            if (energyDestroyed != 0) {
                energyDestroyed--
                updateViews()
            }
        }

        root.findViewById<ImageView>(R.id.increase_energy_gained).setOnClickListener {
            energyGained++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_gained).setOnClickListener {
            if (energyGained != 0) {
                energyGained--
                updateViews()
            }
        }

        root.findViewById<ImageButton>(R.id.close).setOnClickListener {
            onClose()
        }

        next.setOnClickListener {
            history.add(EnergyHistory(currentRound, currentEnergy))
            currentEnergy = nextRoundEnergy()
            energyGained = 0
            energyDestroyed = 0
            energyUsed = 0
            currentRound++
            updateViews()
        }

        reset.setOnClickListener {
            currentEnergy = 3
            energyUsed = 0
            energyDestroyed = 0
            energyGained = 0
            currentRound = 1
            updateViews()
        }

        root.findViewById<ImageButton>(R.id.settings).setOnClickListener {
            it.visibility = View.VISIBLE
            context.startSettingsActivity()
        }
        undo.setOnClickListener { onUndo() }
    }

    private fun undoRound() {
        if (history.isNotEmpty()) {
            currentEnergy = history.last().energy
            currentRound = history.last().round
            energyGained = 0
            energyDestroyed = 0
            energyUsed = 0
            history.removeLast()
            updateViews()
        }
    }

    private fun updateViews() {
        if (!isSubscribed) {
            if (remainingRounds < 0) {
                context.startMainActivity(MainActivity.MESSAGE_ROUNDS_EXHAUSTED)
                FloatingWindowActions.getInstance().listener = this
                return
            }
        }
        tvEnergyGained.text = energyGained.toString()
        tvEnergyDestroyed.text = energyDestroyed.toString()
        tvEnergyUsed.text = energyUsed.toString()
        tvCurrentEnergy.text = "$currentEnergy"
        tvCurrentRound.text = "Round $currentRound"
    }

    init {
        FloatingWindowActions.getInstance().listener = this
        initWindowParams()
        initWindow()
        onColorChanged(color)
        CardCounter(context)
        CardCounterActions.getInstance().listener?.onColorChanged(color)
        Arena(context)
        Menu(context, isSubscribed)
    }

    override fun onOpen() {
        if (isOpen) {
            onClose()
            return
        }
        try {
            initWindowParams()
            windowManager.addView(root, windowParams)
            isOpen = true
        } catch (e: Exception) {
            Toast.makeText(
                root.context,
                "We are having problems showing the popup window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onClose() {
        try {
            windowManager.removeView(root)
            isOpen = false
            FloatingWindowActions.getInstance().listener = this
        } catch (e: Exception) {
        }
    }

    private fun setPosition(x: Int, y: Int) {
        initWindowParams()
        windowParams.x = x
        windowParams.y = y
        updateWindow()
    }

    private fun updateWindow() {
        try {
            windowManager.updateViewLayout(root, windowParams)
        } catch (e: Exception) {
            Toast.makeText(root.context, "Layout did not update correctly", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun nextRoundEnergy(): Int {
        val value = currentEnergy - energyUsed + energyGained - energyDestroyed + 2
        return if (value < 2) 2 else if (value > 10) 10 else value
    }

    override fun onColorChanged(color: Int) {
        val colorState = ColorStateList.valueOf(color)
        content.backgroundTintList = colorState
        reset.backgroundTintList = colorState
        next.backgroundTintList = colorState
    }

    override fun onRoundsRefilled() {
        remainingRounds = MAX_ROUNDS
    }

    override fun onUndo() {
        undoRound()
    }

    companion object {
        private const val windowInputWidth = 180
        private const val windowInputHeight = 265
        const val MAX_ROUNDS = 10
    }
}