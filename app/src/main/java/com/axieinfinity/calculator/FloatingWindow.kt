package com.axieinfinity.calculator

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

class FloatingWindow(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.activity_main, null)
    private val tvEnergyUsed: TextView = rootView.findViewById(R.id.energy_used)
    private val tvEnergyDestroyed: TextView = rootView.findViewById(R.id.energy_destroyed)
    private val tvEnergyGained: TextView = rootView.findViewById(R.id.energy_gained)
    private val tvCardsUsed: TextView = rootView.findViewById(R.id.cards_used)
    private val tvCardsGained: TextView = rootView.findViewById(R.id.cards_gained)
    private val tvCardsDestroyed: TextView = rootView.findViewById(R.id.cards_destroyed)
    private val expandContent: View = rootView.findViewById(R.id.expand_content)
    private val collapseContent: View = rootView.findViewById(R.id.minimize_content)
    private val content: View = rootView.findViewById(R.id.content)
    private val inputContainer: View = rootView.findViewById(R.id.input_container)
    private val tvCurrentEnergy: TextView = rootView.findViewById(R.id.current_energy)
    private val tvCurrentCards: TextView = rootView.findViewById(R.id.current_cards)
    private var energyUsed = 0
    private var energyDestroyed = 0
    private var energyGained = 0
    private var cardsUsed = 0
    private var cardsGained = 0
    private var currentEnergy = 3
    private var currentCards = 6
    private var cardsDestroyed = 0

    private val windowParams = WindowManager.LayoutParams(
        0,
        0,
        0,
        0,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    )

    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }

    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics()
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = (widthInDp * dm.density).toInt()
        params.height = (heightInDp * dm.density).toInt()
        params.x = ((dm.widthPixels - params.width) - (dm.density * 10)).toInt()
        params.y = (dm.heightPixels - params.height) / 2
    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, 50, 50)
    }

    private fun initWindow() {
        rootView.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        expandContent.registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        rootView.findViewById<ImageButton>(R.id.increase_energy_used).setOnClickListener {
            energyUsed++
            updateViews()
        }
        rootView.findViewById<ImageButton>(R.id.reduce_energy_used).setOnClickListener {
            if (energyUsed != 0) {
                energyUsed--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.increase_energy_destroyed).setOnClickListener {
            energyDestroyed++
            updateViews()
        }
        rootView.findViewById<ImageButton>(R.id.reduce_energy_destroyed).setOnClickListener {
            if (energyDestroyed != 0) {
                energyDestroyed--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.increase_energy_gained).setOnClickListener {
                energyGained++
                updateViews()
        }
        rootView.findViewById<ImageButton>(R.id.reduce_energy_gained).setOnClickListener {
            if (energyGained != 0) {
                energyGained--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.increase_cards_used).setOnClickListener {
            cardsUsed++
            updateViews()
        }
        rootView.findViewById<ImageButton>(R.id.reduce_cards_used).setOnClickListener {
            if (cardsUsed != 0) {
                cardsUsed--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.increase_cards_gained).setOnClickListener {
                cardsGained++
                updateViews()
        }
        rootView.findViewById<ImageButton>(R.id.reduce_cards_gained).setOnClickListener {
            if (cardsGained != 0) {
                cardsGained--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.reduce_cards_destroyed).setOnClickListener {
            if (cardsDestroyed != 0) {
                cardsDestroyed--
                updateViews()
            }
        }

        rootView.findViewById<ImageButton>(R.id.increase_cards_destroyed).setOnClickListener {
            cardsDestroyed++
            updateViews()
        }

        rootView.findViewById<ImageButton>(R.id.close).setOnClickListener {
            close()
        }

        rootView.findViewById<TextView>(R.id.calculate).setOnClickListener {
            currentEnergy = nextRoundEnergy()
            currentCards = nextRoundCards()
            updateViews()
        }

        expandContent.setOnClickListener {
            it.visibility = View.GONE
            val dm = getCurrentDisplayMetrics()
            windowParams.width = (windowInputWidth * dm.density).toInt()
            windowParams.height = (windowInputHeight * dm.density).toInt()
            windowParams.x = ((dm.widthPixels - windowParams.width) - (dm.density * 10)).toInt()
            windowParams.y = (dm.density * 20).toInt()
            update()
            inputContainer.visibility = View.VISIBLE
            content.visibility = View.VISIBLE
        }

        collapseContent.setOnClickListener {
            content.visibility = View.GONE
            val dm = getCurrentDisplayMetrics()
            windowParams.width = (50 * dm.density).toInt()
            windowParams.height = (50 * dm.density).toInt()
            windowParams.x = ((dm.widthPixels - windowParams.width) - (dm.density * 10)).toInt()
            update()
            expandContent.visibility = View.VISIBLE
        }

        rootView.findViewById<TextView>(R.id.reset_rounds).setOnClickListener {
            currentEnergy = 3
            currentCards = 6
            energyUsed = 0
            energyDestroyed = 0
            energyGained = 0
            cardsGained = 0
            cardsDestroyed = 0
            cardsUsed = 0
            updateViews()
        }
    }

    private fun updateViews() {
        tvCardsDestroyed.text = cardsDestroyed.toString()
        tvCardsGained.text = cardsGained.toString()
        tvCardsUsed.text = cardsUsed.toString()
        tvEnergyGained.text = energyGained.toString()
        tvEnergyDestroyed.text = energyDestroyed.toString()
        tvEnergyUsed.text = energyUsed.toString()
        tvCurrentEnergy.text = "Energy - $currentEnergy"
        tvCurrentCards.text = "Cards - $currentCards"
    }

    init {
        initWindowParams()
        initWindow()
    }

    fun open() {
        try {
            windowManager.addView(rootView, windowParams)
        } catch (e: Exception) {
            Toast.makeText(
                rootView.context,
                "We are having problems showing a popup window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun close() {
        try {
            windowManager.removeView(rootView)
        } catch (e: Exception) {
            Toast.makeText(
                rootView.context,
                "We are having problems closing this window",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setPosition(x: Int, y: Int) {
        windowParams.x = x
        windowParams.y = y
        update()
    }

    private fun update() {
        try {
            windowManager.updateViewLayout(rootView, windowParams)
        } catch (e: Exception) {
            Toast.makeText(rootView.context, "Layout did not update correctly", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun nextRoundEnergy(): Int {
        val value = currentEnergy - energyUsed + energyGained - energyDestroyed + 2
        return if (value > 10) 10 else value
    }

    private fun nextRoundCards(): Int {
        val value =  currentCards - cardsUsed + cardsGained - cardsDestroyed + 3
        return if (value > 12) 12 else value
    }

    companion object {
        private const val windowInputWidth = 300
        private const val windowInputHeight = 245
    }
}