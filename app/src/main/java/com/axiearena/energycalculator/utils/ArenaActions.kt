package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.ArenaTrackerData

class ArenaActions private constructor() {
    var listener: OnArenaAction? = null
    var arenaData: ArenaTrackerData = ArenaTrackerData()

    interface OnArenaAction {
        fun onOpen()
        fun onClose()
        fun onConfigsChange()
        fun onSoundConfigsChange(isSoundEnabled: Boolean)
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