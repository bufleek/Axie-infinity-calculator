package com.axiearena.energycalculator.utils

class MenuActions private constructor() {
    var listener: OnMenuAction? = null

    interface OnMenuAction {
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
    }

    companion object {
        private var instance: MenuActions? = null
        @JvmStatic
        fun getInstance(): MenuActions{
            if (instance == null){
                instance = MenuActions()
            }
            return instance!!
        }
    }
}