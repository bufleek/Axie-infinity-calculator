package com.axieinfinity.calculator.utils

class ArenaActions private constructor() {
    var listener: OnArenaAction? = null

    interface OnArenaAction {
        fun onOpen()
        fun onClose()
    }

    companion object {
        private var instance: ArenaActions? = null
        @JvmStatic
        fun getInstance(): ArenaActions{
            if (instance == null){
                instance = ArenaActions()
            }
            return instance!!
        }
    }
}