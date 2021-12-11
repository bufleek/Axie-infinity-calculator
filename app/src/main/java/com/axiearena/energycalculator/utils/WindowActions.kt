package com.axiearena.energycalculator.utils

class WindowActions private constructor() {
    var listener: OnWindowAction? = null

    interface OnWindowAction {
        fun onConfigsChange()
    }

    companion object {
        private var instance: WindowActions? = null
        @JvmStatic
        fun getInstance(): WindowActions{
            if (instance == null){
                instance = WindowActions()
            }
            return instance!!
        }
    }
}