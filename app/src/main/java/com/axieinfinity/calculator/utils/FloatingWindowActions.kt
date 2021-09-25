package com.axieinfinity.calculator.utils

class FloatingWindowActions private constructor() {
    var listener: OnFloatingWindowAction? = null

    interface OnFloatingWindowAction {
        fun onColorChanged(color: Int)
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