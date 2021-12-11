package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.SlpCalculatorData

class SlpActions private constructor() {
    var listener: OnSlpAction? = null
    var slpCalculatorData = SlpCalculatorData()

    interface OnSlpAction {
        fun onOpen()
        fun onClose()
        fun onColorChanged(color: Int)
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: SlpActions? = null
        @JvmStatic
        fun getInstance(): SlpActions{
            if (instance == null){
                instance = SlpActions()
            }
            return instance!!
        }
    }
}