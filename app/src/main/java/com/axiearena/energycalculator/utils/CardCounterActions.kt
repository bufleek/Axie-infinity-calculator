package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.CardCounterData

class CardCounterActions private constructor(){
    var listener: OnCardCounterActions? = null
    var cardCounterData = CardCounterData()

    interface OnCardCounterActions {
        fun onOpen()
        fun onClose()
        fun onColorChanged(color: Int)
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: CardCounterActions? = null
        @JvmStatic
        fun getInstance(): CardCounterActions{
            if (instance == null){
                instance = CardCounterActions()
            }
            return instance!!
        }
    }
}