package com.axiearena.energycalculator.ui.floating_windows

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.axiearena.energycalculator.utils.getCurrentDisplayMetrics
import com.axiearena.energycalculator.utils.playSound
import com.axiearena.energycalculator.utils.CardCounterActions
import com.axiearena.energycalculator.utils.registerDraggableTouchListener
import com.axiearena.energycalculator.utils.windowParams

class CardCounter(
    private val context: Context,
    private val isPcMode: Boolean = false,
    session: Session? = null,
) : CardCounterActions.OnCardCounterActions {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val root = layoutInflater.inflate(if (isPcMode) R.layout.item_card_counter_pc else R.layout.item_card_counter, null)
    private val tvCurrentCards: TextView = root.findViewById(R.id.current_cards)
    private val tvUpperRightNumber: TextView = root.findViewById(R.id.upper_right_number)
    private val addUpperRightNumber: ImageView = root.findViewById(R.id.add_upper_right_number)
    private val reduceUpperRightNumber: ImageView =
        root.findViewById(R.id.reduce_upper_right_number)
    private val tvUsedCards: TextView = root.findViewById(R.id.used_card)
    private val addUsedCards: ImageView = root.findViewById(R.id.add_used_card)
    private val reduceUsedCards: ImageView = root.findViewById(R.id.reduce_used_card)
    private val reset: TextView = root.findViewById(R.id.reset)
    private val calculate: TextView = root.findViewById(R.id.calculate)
    private val imgClose: ImageButton = root.findViewById(R.id.img_close)
    private val topFrame: FrameLayout = root.findViewById(R.id.top_frame)
    private var currentCards = session?.cardCounterData?.cards ?: 3
    private var cardLastRound = 0
    private var usedCards = 0
    private var isOpen = false
    private var isSoundEnabled = false

    init {
        initWindowParams()
        CardCounterActions.getInstance().listener = this
        initWindow()
        session?.cardCounterData?.cards?.let {
            tvCurrentCards.text = "$it"
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

    private fun updateViews() {
        tvUsedCards.text = usedCards.toString()
        tvUpperRightNumber.text = cardLastRound.toString()
    }

    private fun initWindow() {
        imgClose.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            onClose()
        }

        if (isPcMode) {
            topFrame.visibility = View.GONE
            topFrame.background = null
            root.background = null
            reset.background = null
            calculate.background = null
        } else {
            root.registerDraggableTouchListener(
                initialPosition = { Point(windowParams.x, windowParams.y) },
                positionListener = { x, y -> setPosition(x, y) }
            )
        }
        addUpperRightNumber.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            cardLastRound++
            updateViews()
        }
        reduceUpperRightNumber.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            if (cardLastRound > 0) {
                cardLastRound--
                updateViews()
            }
        }
        addUsedCards.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            usedCards++
            updateViews()
        }
        reduceUsedCards.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            if (usedCards > 0) {
                usedCards--
                updateViews()
            }
        }
        reset.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            tvCurrentCards.text = "-"
            usedCards = 0
            cardLastRound = 0
            updateViews()
        }

        calculate.setOnClickListener {
            if (isSoundEnabled) {
                context.playSound()
            }

            val calc = cardLastRound - usedCards + 3
            currentCards = if (calc >= 12) 12 else if (calc <= 3) 3 else calc
            tvCurrentCards.text = currentCards.toString()
            usedCards = 0
            cardLastRound = 0
            updateViews()
        }
        updateViews()
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
        if (!isPcMode){
            calculateSizeAndPosition(windowParams, WIDTH, HEIGHT)
        }
    }

    companion object {
        private const val WIDTH = 180
        private const val HEIGHT = 230
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
            CardCounterActions.getInstance().listener = this
        } catch (e: Exception) {
        }
    }

    override fun onClose() {
        try {
            windowManager.removeView(root)
            isOpen = false
        } catch (e: Exception) {
        }
        CardCounterActions.getInstance().cardCounterData.cards = currentCards
    }

    override fun onColorChanged(color: Int) {
        val colorState = ColorStateList.valueOf(color)
        topFrame.backgroundTintList = colorState
        reset.backgroundTintList = colorState
        calculate.backgroundTintList = colorState
        root.backgroundTintList = colorState
    }

    override fun onSoundConfigsChange(isSoundEnabled: Boolean) {
        this.isSoundEnabled = isSoundEnabled
    }
}