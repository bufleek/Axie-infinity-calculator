package com.axiearena.energycalculator.ui.floating_windows

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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import com.axiearena.energycalculator.*
import com.axiearena.energycalculator.data.models.EnergyHistory
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.ui.main.MainActivity
import com.axiearena.energycalculator.utils.*

class FloatingWindow(
    private val context: Context,
    private val color: Int,
    private val isSubscribed: Boolean = true,
    private val isPcMode: Boolean = false,
    private val isBasicMode: Boolean = false,
    private val session: Session? = null
) : FloatingWindowActions.OnFloatingWindowAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root = layoutInflater.inflate(
        if (isPcMode) R.layout.item_main_popup_pc else R.layout.item_main_popup,
        null
    )
    val thumb = root.findViewById<CardView>(R.id.thumb_menu)
    private val tvEnergyUsed: TextView = root.findViewById(R.id.energy_used)
    private val tvEnergyDestroyed: TextView = root.findViewById(R.id.energy_destroyed)
    private val tvEnergyGained: TextView = root.findViewById(R.id.energy_gained)
    private val content: View = root.findViewById(R.id.content)
    private val tvCurrentEnergy: TextView = root.findViewById(R.id.current_energy)
    private val tvCurrentRound: TextView = root.findViewById(R.id.current_round)
    private val reset = root.findViewById<TextView>(R.id.reset_rounds)
    private val next = root.findViewById<TextView>(R.id.calculate)
    private val undo = root.findViewById<ImageView>(R.id.img_undo)
    private val top = root.findViewById<View>(R.id.top)
    private val help = root.findViewById<ImageButton>(R.id.help)
    private val blockHelp = root.findViewById<ImageButton>(R.id.block_help)
    private val helpersGroup = root.findViewById<Group>(R.id.helper_group)
    private val minimize = root.findViewById<ImageButton>(R.id.minimize)
    private var energyUsed = 0
    private var energyDestroyed = 0
    private var energyGained = 0
    private var currentEnergy = session?.energyCalcData?.energy ?: 3
    private var currentRound = session?.energyCalcData?.round ?: 1
    private var remainingRounds = MAX_ROUNDS
    private var history: ArrayList<EnergyHistory> = session?.energyCalcData?.history ?: ArrayList()
    private var isOpen = false
    private var isSoundEnabled = false

    init {
        FloatingWindowActions.getInstance().listener = this
        initWindow()
        if (!isPcMode) {
            initWindowParams()
            if (isBasicMode) {
                onColorChanged(color)
            } else {
                onColorChanged(color)
                CardCounter(context, false, session = session)
                Arena(context, false, session = session)
                Menu(context, isSubscribed)
                SLPCalculator(context, color, false, session = session)
                PvpDamage(context)
                CardCounterActions.getInstance().listener?.onColorChanged(color)
            }
        }
    }

    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics(windowManager)
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = (widthInDp * dm.density).toInt()
        params.height = (heightInDp * dm.density).toInt()
        if (thumb.visibility != View.GONE) {
            params.x =
                ((dm.widthPixels - params.width) - (dm.density * 10)).toInt()
            params.y =
                if ((dm.density * 100) < (dm.density * Arena.ARENA_HEIGHT)) ((dm.density * Arena.ARENA_HEIGHT + 20).toInt()) else params.y
        } else {
            params.x = 0
            params.y = 0
        }
    }

    private fun initWindowParams() {
        if (isBasicMode && thumb.visibility != View.GONE) {
            calculateSizeAndPosition(windowParams, thumbMenuWidth, thumbMenuHeight)
        } else {
            calculateSizeAndPosition(windowParams, windowInputWidth, windowInputHeight)
        }
    }

    private fun initWindow() {
        if (isPcMode) {
            top.visibility = View.GONE
            content.background = null
            reset.background = null
            next.background = null
        } else {
            if (isBasicMode) {
                minimize.visibility = View.VISIBLE
                thumb.visibility = View.VISIBLE
                content.visibility = View.GONE
                thumb.registerDraggableTouchListener(
                    initialPosition = { Point(windowParams.x, windowParams.y) },
                    positionListener = { x, y -> setPosition(x, y) }
                )
                thumb.setOnClickListener {
                    if (isSoundEnabled) {
                        context.playSound()
                    }
                    content.visibility = View.VISIBLE
                    thumb.visibility = View.GONE
                    initWindowParams()
                    updateWindow()
                }
                minimize.setOnClickListener {
                    if (isSoundEnabled) {
                        context.playSound()
                    }
                    content.visibility = View.GONE
                    thumb.visibility = View.VISIBLE
                    initWindowParams()
                    updateWindow()
                }
                onOpen()
            }
            root.registerDraggableTouchListener(
                initialPosition = { Point(windowParams.x, windowParams.y) },
                positionListener = { x, y -> setPosition(x, y) }
            )
        }
        root.findViewById<ImageView>(R.id.increase_energy_used).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            energyUsed++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_used).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            if (energyUsed != 0) {
                energyUsed--
                updateViews()
            }
        }

        root.findViewById<ImageView>(R.id.increase_energy_destroyed).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            energyDestroyed++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_destroyed).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            if (energyDestroyed != 0) {
                energyDestroyed--
                updateViews()
            }
        }

        root.findViewById<ImageView>(R.id.increase_energy_gained).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            energyGained++
            updateViews()
        }
        root.findViewById<ImageView>(R.id.reduce_energy_gained).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            if (energyGained != 0) {
                energyGained--
                updateViews()
            }
        }

        root.findViewById<ImageButton>(R.id.close).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            onClose()
            if (isBasicMode) {
                FloatingWindowActions.getInstance().listener = null
            }
        }

        next.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            history.add(EnergyHistory(currentRound, currentEnergy))
            currentEnergy = nextRoundEnergy()
            energyGained = 0
            energyDestroyed = 0
            energyUsed = 0
            currentRound++
            updateViews()
        }

        reset.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            currentEnergy = 3
            energyUsed = 0
            energyDestroyed = 0
            energyGained = 0
            currentRound = 1
            updateViews()
        }

        root.findViewById<ImageButton>(R.id.minimize).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            content.visibility = View.GONE
            thumb.visibility = View.VISIBLE
            initWindowParams()
            updateWindow()
        }
        undo.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            onUndo()
        }

        help.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            helpersGroup.visibility = View.VISIBLE
            blockHelp.visibility = View.VISIBLE
        }
        blockHelp.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            helpersGroup.visibility = View.GONE
            blockHelp.visibility = View.GONE
        }

        updateViews()
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

    override fun onOpen() {
        if (!isPcMode) {
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
    }

    override fun onClose() {
        if (!isPcMode) {
            try {
                windowManager.removeView(root)
                isOpen = false
                FloatingWindowActions.getInstance().listener = this
            } catch (e: Exception) {
            }
        }
        FloatingWindowActions.getInstance().energyCalcData.apply {
            energy = currentEnergy
            round = currentRound
            history = this@FloatingWindow.history
        }
        ServiceActions.getInstance().listener?.onBackUpSession(
            Session(
                System.currentTimeMillis(),
                isPcMode,
                isBasicMode,
                FloatingWindowActions.getInstance().energyCalcData,
                CardCounterActions.getInstance().cardCounterData,
                ArenaActions.getInstance().arenaData,
                SlpActions.getInstance().slpCalculatorData
            )
        )
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
        top.backgroundTintList = colorState
    }

    override fun onRoundsRefilled() {
        remainingRounds = MAX_ROUNDS
    }

    override fun onUndo() {
        undoRound()
    }

    override fun onSoundConfigChanged(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
        ArenaActions.getInstance().listener?.onSoundConfigsChange(isSoundEnabled)
        CardCounterActions.getInstance().listener?.onSoundConfigsChange(isSoundEnabled)
        MenuActions.getInstance().listener?.onSoundConfigsChange(isSoundEnabled)
        PvpActions.getInstance().listener?.onSoundConfigsChange(isSoundEnabled)
        SlpActions.getInstance().listener?.onSoundConfigsChange(isSoundEnabled)
    }

    companion object {
        private const val windowInputWidth = 180
        private const val windowInputHeight = 265
        private const val thumbMenuWidth = 50
        private const val thumbMenuHeight = 50
        const val MAX_ROUNDS = 10
    }
}