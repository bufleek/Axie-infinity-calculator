package com.axiearena.energycalculator.ui.floating_windows

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.utils.getCurrentDisplayMetrics
import com.axiearena.energycalculator.utils.playSound
import com.axiearena.energycalculator.utils.ArenaActions
import com.axiearena.energycalculator.utils.windowParams

class Arena(private val context: Context, private val isPcMode: Boolean = false, session: Session? = null) :
    ArenaActions.OnArenaAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root =
        layoutInflater.inflate(if (isPcMode) R.layout.item_arena_pc else R.layout.item_arena, null)
    private val tvWon: TextView = root.findViewById(R.id.won)
    private val imgWon: ImageView = root.findViewById(R.id.img_won)
    private val tvLost: TextView = root.findViewById(R.id.lost)
    private val imgLost: ImageView = root.findViewById(R.id.img_lost)
    private val tvDrawn: TextView = root.findViewById(R.id.drawn)
    private val imgDrawn: ImageView = root.findViewById(R.id.img_drawn)
    private val imgReset: ImageView = root.findViewById(R.id.img_reset)
    private var won = session?.arenaTrackerData?.win ?: 0
    private var lost = session?.arenaTrackerData?.loss ?: 0
    private var drawn = session?.arenaTrackerData?.draw ?: 0
    private var isOpen = false
    private var isSoundEnabled = false

    init {
        ArenaActions.getInstance().listener = this
        initWindowParams()
        initializeArena()
    }

    private fun updateArena() {
        tvWon.text = "$won"
        tvDrawn.text = "$drawn"
        tvLost.text = "$lost"
    }

    private fun initializeArena() {
        if (isPcMode) {
            root.visibility = View.VISIBLE
            root.background = null
        }
        tvWon.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            if (won > 0) {
                won--
                updateArena()
            }
        }
        tvLost.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            if (lost > 0) {
                lost--
                updateArena()
            }
        }
        tvDrawn.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()

            }
            if (drawn > 0) {
                drawn--
                updateArena()
            }
        }
        imgWon.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            won++
            updateArena()
        }
        imgLost.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            lost++
            updateArena()
        }
        imgDrawn.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            drawn++
            updateArena()
        }
        imgReset.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            drawn = 0
            won = 0
            lost = 0
            updateArena()
        }

        updateArena()
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
        params.x = 0
        params.y = 0
    }

    private fun initWindowParams() {
        if (!isPcMode) {
            calculateSizeAndPosition(windowParams, ARENA_WIDTH, ARENA_HEIGHT)
        }
    }

    override fun onOpen() {
        if (!isPcMode && isOpen) {
            onClose()
            return
        }
        try {
            initWindowParams()
            windowManager.addView(root, windowParams)
            root.visibility = View.VISIBLE
            isOpen = true
        } catch (e: Exception) {
            Toast.makeText(
                root.context,
                "We are having problems showing arena window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onClose() {
        if (!isPcMode) {
            try {
                windowManager.removeView(root)
                isOpen = false
            } catch (e: Exception) {

            }
        }
        ArenaActions.getInstance().arenaData.apply {
            win = won
            loss = lost
            draw = drawn
        }
    }

    override fun onConfigsChange() {
        initWindowParams()
        updateArena()
    }

    override fun onSoundConfigsChange(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
    }

    companion object {
        const val ARENA_HEIGHT = 172
        private const val ARENA_WIDTH = 96
    }
}