package com.axieinfinity.energycalculator.ui.floating_windows

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.axieinfinity.energycalculator.R
import com.axieinfinity.energycalculator.getCurrentDisplayMetrics
import com.axieinfinity.energycalculator.utils.ArenaActions
import com.axieinfinity.energycalculator.windowParams

class Arena(context: Context) : ArenaActions.OnArenaAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val root = layoutInflater.inflate(R.layout.item_arena, null)
    private val tvWon: TextView = root.findViewById(R.id.won)
    private val imgWon: ImageView = root.findViewById(R.id.img_won)
    private val tvLost: TextView = root.findViewById(R.id.lost)
    private val imgLost: ImageView = root.findViewById(R.id.img_lost)
    private val tvDrawn: TextView = root.findViewById(R.id.drawn)
    private val imgDrawn: ImageView = root.findViewById(R.id.img_drawn)
    private val imgReset: ImageView = root.findViewById(R.id.img_reset)
    private var won = 0
    private var lost = 0
    private var drawn = 0
    private var isOpen = false

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
        tvWon.setOnClickListener {
            if (won > 0) {
                won--
                updateArena()
            }
        }
        tvLost.setOnClickListener {
            if (lost > 0) {
                lost--
                updateArena()
            }
        }
        tvDrawn.setOnClickListener {
            if (drawn > 0) {
                drawn--
                updateArena()
            }
        }
        imgWon.setOnClickListener {
            won++
            updateArena()
        }
        imgLost.setOnClickListener {
            lost++
            updateArena()
        }
        imgDrawn.setOnClickListener {
            drawn++
            updateArena()
        }
        imgReset.setOnClickListener {
            drawn = 0
            won = 0
            lost = 0
            updateArena()
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
        params.x = 0
        params.y = 0
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, ARENA_WIDTH, ARENA_HEIGHT)
    }

    override fun onOpen() {
        if (isOpen) {
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
        try {
            windowManager.removeView(root)
            isOpen = false
        } catch (e: Exception) {

        }
    }

    companion object {
        const val ARENA_HEIGHT = 172
        private const val ARENA_WIDTH = 96
    }
}