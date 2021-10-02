package com.axieinfinity.energycalculator.ui.floating_windows

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import com.axieinfinity.energycalculator.R
import com.axieinfinity.energycalculator.getCurrentDisplayMetrics
import com.axieinfinity.energycalculator.utils.CardCounterActions
import com.axieinfinity.energycalculator.utils.registerDraggableTouchListener
import com.axieinfinity.energycalculator.windowParams

class CardCounter(
    private val context: Context,
) : CardCounterActions.OnCardCounterActions {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val root = layoutInflater.inflate(R.layout.item_card_counter, null)
    private val tvCurrentCards: TextView = root.findViewById(R.id.current_cards)
    private val tvUpperRightNumber: TextView = root.findViewById(R.id.upper_right_number)
    private val addUpperRightNumber: ImageView = root.findViewById(R.id.add_upper_right_number)
    private val reduceUpperRightNumber: ImageView =
        root.findViewById(R.id.reduce_upper_right_number)
    private val tvUsedCards: TextView = root.findViewById(R.id.used_card)
    private val addUsedCards: ImageView = root.findViewById(R.id.add_used_card)
    private val reduceUsedCards: ImageView = root.findViewById(R.id.reduce_used_card)
    private val tvDrawCards: TextView = root.findViewById(R.id.draw_card)
    private val addDrawCards: ImageView = root.findViewById(R.id.add_draw_card)
    private val reduceDrawCards: ImageView = root.findViewById(R.id.reduce_draw_card)
    private val tvDiscardCards: TextView = root.findViewById(R.id.discard_card)
    private val addDiscardCards: ImageView = root.findViewById(R.id.add_discard_card)
    private val reduceDiscardCards: ImageView = root.findViewById(R.id.reduce_discard_card)
    private val reset: TextView = root.findViewById(R.id.reset)
    private val calculate: TextView = root.findViewById(R.id.calculate)
    private val imgClose: ImageButton = root.findViewById(R.id.img_close)
    private val topFrame: FrameLayout = root.findViewById(R.id.top_frame)
    private var currentCards = 3
    private var upperRightNumber = 0
    private var usedCards = 0
    private var drawCards = 0
    private var discardCards = 0
    private var isOpen = false

    init {
        initWindowParams()
        CardCounterActions.getInstance().listener = this
        initWindow()
        updateViews()
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
        tvDrawCards.text = drawCards.toString()
        tvDiscardCards.text = discardCards.toString()
        tvUpperRightNumber.text = upperRightNumber.toString()
    }

    private fun initWindow() {
        imgClose.setOnClickListener { onClose() }

        root.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        addUpperRightNumber.setOnClickListener {
            upperRightNumber++
            updateViews()
        }
        reduceUpperRightNumber.setOnClickListener {
            if (upperRightNumber > 0) {
                upperRightNumber--
                updateViews()
            }
        }
        addUsedCards.setOnClickListener {
            usedCards++
            updateViews()
        }
        reduceUsedCards.setOnClickListener {
            if (usedCards > 0) {
                usedCards--
                updateViews()
            }
        }
        addDrawCards.setOnClickListener {
            drawCards++
            updateViews()
        }
        reduceDrawCards.setOnClickListener {
            if (drawCards > 0) {
                drawCards--
                updateViews()
            }
        }
        addDiscardCards.setOnClickListener {
            discardCards++
            updateViews()
        }
        reduceDiscardCards.setOnClickListener {
            if (discardCards > 0) {
                discardCards--
                updateViews()
            }
        }
        reset.setOnClickListener {
            currentCards = 3
            tvCurrentCards.text = currentCards.toString()
            usedCards = 0
            drawCards = 0
            discardCards = 0
            upperRightNumber = 0
            updateViews()
        }

        calculate.setOnClickListener {
            val calc = upperRightNumber - usedCards + drawCards - discardCards
            currentCards = if (calc >= 12) 12 else if (calc <= 3) 3 else calc
            tvCurrentCards.text = currentCards.toString()
            usedCards = 0
            drawCards = 0
            discardCards = 0
            upperRightNumber = 0
            updateViews()
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
        params.x =
            ((dm.widthPixels - windowParams.width) - (dm.density * (Menu.MENU_WIDTH + 0))).toInt()
        params.y = 0
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, WIDTH, HEIGHT)
    }

    companion object {
        private const val WIDTH = 180
        private const val HEIGHT = 310
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
            Toast.makeText(
                root.context,
                "We are having problems closing card counter window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onColorChanged(color: Int) {
        topFrame.setBackgroundColor(color)
        reset.setBackgroundColor(color)
        calculate.setBackgroundColor(color)
        root.setBackgroundColor(color)
    }
}