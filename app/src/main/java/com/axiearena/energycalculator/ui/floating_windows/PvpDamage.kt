package com.axiearena.energycalculator.ui.floating_windows

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.utils.getCurrentDisplayMetrics
import com.axiearena.energycalculator.utils.playSound
import com.axiearena.energycalculator.utils.PvpActions
import com.axiearena.energycalculator.utils.registerDraggableTouchListener
import com.axiearena.energycalculator.utils.windowParams

class PvpDamage(
    private val context: Context,
    private val color: Int
) : PvpActions.OnPvpAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root = layoutInflater.inflate(R.layout.item_pvp_damage, null)
    private var isOpen = false
    private var isSoundEnabled: Boolean = false

    init {
        PvpActions.getInstance().listener = this
        initWindowParams()
        initWindow()
        onColorChanged(color)
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
        params.x = ((dm.widthPixels - params.width) / 2).toInt()
        params.y = ((dm.heightPixels - params.height) / 2).toInt()
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(
            windowParams,
            windowInputWidth,
            windowInputHeight
        )
    }

    private fun initWindow() {
        root.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        root.findViewById<ImageButton>(R.id.pvp_close).setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            onClose()
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

    override fun onOpen() {
        if (isOpen) {
            onClose()
        } else {
            try {
                initWindowParams()
                windowManager.addView(root, windowParams)
                PvpActions.getInstance().listener = this
                isOpen = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to start pvp", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onClose() {
        try {
            windowManager.removeView(root)
            PvpActions.getInstance().listener = this
            isOpen = false
        } catch (e: Exception) {
        }
    }

    override fun onSoundConfigsChange(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
    }

    override fun onColorChanged(color: Int) {
        val colorState = ColorStateList.valueOf(color)
        root.backgroundTintList = colorState
    }

    companion object {
        private const val windowInputHeight = 270
        private const val windowInputWidth = 270
    }
}