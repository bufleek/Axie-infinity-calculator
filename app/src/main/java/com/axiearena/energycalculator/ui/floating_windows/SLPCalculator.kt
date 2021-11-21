package com.axiearena.energycalculator.ui.floating_windows

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.utils.getCurrentDisplayMetrics
import com.axiearena.energycalculator.utils.playSound
import com.axiearena.energycalculator.utils.SlpActions
import com.axiearena.energycalculator.utils.registerDraggableTouchListener
import com.axiearena.energycalculator.utils.windowParams

class SLPCalculator(
    private val context: Context,
    private val color: Int,
    private val isPcMode: Boolean = false,
    session: Session? = null
) : SlpActions.OnArenaAction {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root = layoutInflater.inflate(if(isPcMode) R.layout.item_slp_calculator_pc else R.layout.item_slp_calculator, null)
    private var isOpen = false
    private var isSoundEnabled = false

    private val tvSlpHistory: TextView = root.findViewById(R.id.tv_slp_history)
    private val tvSlp: TextView = root.findViewById(R.id.tv_slp)
    private val tvSlp1: TextView = root.findViewById(R.id.tv_slp_1)
    private val tvSlp3: TextView = root.findViewById(R.id.tv_slp_3)
    private val tvSlp6: TextView = root.findViewById(R.id.tv_slp_6)
    private val tvSlp9: TextView = root.findViewById(R.id.tv_slp_9)
    private val tvSlp12: TextView = root.findViewById(R.id.tv_slp_12)
    private val tvSlp15: TextView = root.findViewById(R.id.tv_slp_15)
    private val tvSlp18: TextView = root.findViewById(R.id.tv_slp_18)
    private val tvSlp21: TextView = root.findViewById(R.id.tv_slp_21)
    private val tvSlp24: TextView = root.findViewById(R.id.tv_slp_24)
    private val tvSlpUndo: TextView = root.findViewById(R.id.tv_slp_undo)
    private val tvSlpReset: TextView = root.findViewById(R.id.tv_slp_reset)

    private val slpHistoryList = session?.slpCalculatorData?.history ?: ArrayList()

    init {
        SlpActions.getInstance().listener = this
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
        params.x = 0
//            ((dm.widthPixels - params.width) - (dm.density * 10)).toInt()
        params.y = 0
//            if ((dm.density * 100) < (dm.density * Arena.ARENA_HEIGHT)) ((dm.density * Arena.ARENA_HEIGHT + 20).toInt()) else params.y
    }

    private fun initWindowParams() {
        if (!isPcMode) {
            calculateSizeAndPosition(
                windowParams,
                windowInputWidth,
                windowInputHeight
            )
        }
    }

    private fun initWindow() {
        if (!isPcMode) {
            root.registerDraggableTouchListener(
                initialPosition = { Point(windowParams.x, windowParams.y) },
                positionListener = { x, y -> setPosition(x, y) }
            )
        } else {
            root.background = null
        }

        tvSlp1.setOnClickListener { addSlp(1) }

        tvSlp3.setOnClickListener { addSlp(3) }

        tvSlp6.setOnClickListener { addSlp(6) }

        tvSlp9.setOnClickListener { addSlp(9) }

        tvSlp12.setOnClickListener { addSlp(12) }

        tvSlp15.setOnClickListener { addSlp(15) }

        tvSlp18.setOnClickListener { addSlp(18) }

        tvSlp21.setOnClickListener { addSlp(21) }

        tvSlp24.setOnClickListener { addSlp(24) }

        tvSlpReset.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            slpHistoryList.clear()
            updateViewData()
        }

        tvSlpUndo.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }
            slpHistoryList.removeLastOrNull()
            updateViewData()
        }

        updateViewData()
    }

    private fun addSlp(slp: Int) {
        if (isSoundEnabled) {
            context.playSound()
        }
        slpHistoryList.add(slp)
        updateViewData()
    }

    private fun updateViewData() {
        tvSlp.text = slpHistoryList.sum().toString()
        tvSlpHistory.text = if (slpHistoryList.isEmpty()) "--" else slpHistoryList.joinToString(
            separator = "+",
            prefix = "+"
        ) { it.toString() }
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
                SlpActions.getInstance().listener = this
                isOpen = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to start slp", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onClose() {
        try {
            windowManager.removeView(root)
            SlpActions.getInstance().listener = this
            isOpen = false
        } catch (e: Exception) {
        }
        SlpActions.getInstance().slpCalculatorData.apply {
            history = slpHistoryList
        }
    }

    override fun onColorChanged(color: Int) {
        val colorState = ColorStateList.valueOf(color)
        root.backgroundTintList = colorState
    }

    override fun onSoundConfigsChange(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
    }

    companion object {
        private const val windowInputHeight = 214
        private const val windowInputWidth = 224
    }
}