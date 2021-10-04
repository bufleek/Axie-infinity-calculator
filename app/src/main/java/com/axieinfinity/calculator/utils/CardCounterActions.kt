package com.axieinfinity.calculator.utils

class CardCounterActions private constructor(){
    var listener: OnCardCounterActions? = null

    interface OnCardCounterActions {
        fun onOpen()
        fun onClose()
        fun onColorChanged(color: Int)
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