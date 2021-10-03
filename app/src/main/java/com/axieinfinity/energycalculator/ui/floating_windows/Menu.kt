package com.axieinfinity.energycalculator.ui.floating_windows

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.axieinfinity.energycalculator.R
import com.axieinfinity.energycalculator.getCurrentDisplayMetrics
import com.axieinfinity.energycalculator.utils.ArenaActions
import com.axieinfinity.energycalculator.utils.CardCounterActions
import com.axieinfinity.energycalculator.utils.FloatingWindowActions
import com.axieinfinity.energycalculator.utils.registerDraggableTouchListener
import com.axieinfinity.energycalculator.windowParams

class Menu(private val context: Context, private val isSubscribed: Boolean) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val root = layoutInflater.inflate(R.layout.item_menu, null)
    private val imgClose: ImageView = root.findViewById(R.id.img_close)
    private val cardArena: CardView = root.findViewById(R.id.card_arena)
    private val cardCardCounter: CardView = root.findViewById(R.id.card_card_counter)
    private val cardEnergyCalc: CardView = root.findViewById(R.id.card_energy_calc)

    init {
        initWindowParams()
        initMenu()
        open()
    }

    private fun initMenu(){
        cardCardCounter.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        cardArena.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        imgClose.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        cardEnergyCalc.registerDraggableTouchListener(
            initialPosition = { Point(root.x.toInt(), windowParams.y.toInt()) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        imgClose.setOnClickListener {
            ArenaActions.getInstance().listener?.onClose()
            FloatingWindowActions.getInstance().listener?.onClose()
            FloatingWindowActions.getInstance().listener = null
            CardCounterActions.getInstance().listener?.onClose()
            CardCounterActions.getInstance().listener = null
            windowManager.removeView(root)
        }
        cardArena.setOnClickListener { ArenaActions.getInstance().listener?.onOpen() }
        cardCardCounter.setOnClickListener { CardCounterActions.getInstance().listener?.onOpen() }
        cardEnergyCalc.setOnClickListener { FloatingWindowActions.getInstance().listener?.onOpen() }
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

    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics(windowManager)
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = (widthInDp * dm.density).toInt()
        params.height = (heightInDp * dm.density).toInt()
        params.x = ((dm.widthPixels - windowParams.width) - (dm.density * 10)).toInt()
        params.y = 0
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, MENU_WIDTH, MENU_HEIGHT)
    }

    private fun open() {
        try {
            initWindowParams()
            windowManager.addView(root, windowParams)
            root.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(
                root.context,
                "We are having problems showing arena window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object{
        private const val MENU_HEIGHT = 184
        const val MENU_WIDTH = 35
    }
}