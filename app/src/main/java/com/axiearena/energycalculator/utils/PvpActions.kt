package com.axiearena.energycalculator.utils

class PvpActions private constructor() {
    var listener: OnPvpAction? = null

    interface OnPvpAction {
        fun onOpen()
        fun onClose()
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: PvpActions? = null
        @JvmStatic
        fun getInstance(): PvpActions{
            if (instance == null){
                instance = PvpActions()
            }
            return instance!!
        }
    }
}