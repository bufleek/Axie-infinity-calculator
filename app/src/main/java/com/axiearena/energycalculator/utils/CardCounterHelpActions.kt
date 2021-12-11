package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.CardCounterData

class CardCounterHelpActions private constructor(){
    var listener: OnCardCounterHelpActions? = null

    interface OnCardCounterHelpActions {
        fun onOpen()
        fun onClose()
        fun onColorChanged(color: Int)
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: CardCounterHelpActions? = null
        @JvmStatic
        fun getInstance(): CardCounterHelpActions{
            if (instance == null){
                instance = CardCounterHelpActions()
            }
            return instance!!
        }
    }
}