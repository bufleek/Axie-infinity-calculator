package com.axieinfinity.energycalculator.utils

class FloatingWindowActions private constructor() {
    var listener: OnFloatingWindowAction? = null

    interface OnFloatingWindowAction {
        fun onColorChanged(color: Int)
        fun onRoundsRefilled()
        fun onUndo()
        fun onOpen()
        fun onClose()
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