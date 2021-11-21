package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.EnergyCalcData

class FloatingWindowActions private constructor() {
    var listener: OnFloatingWindowAction? = null
    var energyCalcData: EnergyCalcData = EnergyCalcData()

    interface OnFloatingWindowAction {
        fun onColorChanged(color: Int)
        fun onRoundsRefilled()
        fun onUndo()
        fun onOpen()
        fun onClose()
        fun onSoundConfigChanged(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: FloatingWindowActions? = null
        @JvmStatic
        fun getInstance(): FloatingWindowActions{
            if (instance == null){
                instance = FloatingWindowActions()
            }
            return instance!!
        }
    }
}