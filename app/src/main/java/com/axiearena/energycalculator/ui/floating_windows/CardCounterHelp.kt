package com.axiearena.energycalculator.ui.floating_windows

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.utils.*

class CardCounterHelp(
    private val context: Context
) : CardCounterHelpActions.OnCardCounterHelpActions {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root = layoutInflater.inflate(R.layout.item_card_counter_help, null)
    private val imgClose: ImageButton = root.findViewById(R.id.img_close)
    private val topFrame: FrameLayout = root.findViewById(R.id.top_frame)
    private var isOpen = false
    private var isSoundEnabled = false

    init {
        initWindowParams()
        CardCounterHelpActions.getInstance().listener = this
        initWindow()
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

    private fun initWindow() {
        imgClose.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            onClose()
        }

        root.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
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
        //((dm.widthPixels - params.width) - (dm.density * 10)).toInt()
        params.x = 0
        params.y = 0
//            if ((dm.density * 100) < (dm.density * Arena.ARENA_HEIGHT)) ((dm.density * Arena.ARENA_HEIGHT + 20).toInt()) else params.y
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, WIDTH, HEIGHT)
    }

    companion object {
        private const val WIDTH = 190
        private var HEIGHT = 300
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
            CardCounterHelpActions.getInstance().listener = this
        } catch (e: Exception) {
        }
    }

    override fun onClose() {
        try {
            windowManager.removeView(root)
            isOpen = false
        } catch (e: Exception) {
        }
    }

    override fun onColorChanged(color: Int) {
        val colorState = ColorStateList.valueOf(color)
        topFrame.backgroundTintList = colorState
        root.backgroundTintList = colorState
    }

    override fun onSoundConfigsChange(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
    }
}