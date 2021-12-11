package com.axiearena.energycalculator.utils

import com.axiearena.energycalculator.data.models.Session

class ServiceActions private constructor() {
    var listener: OnServiceAction? = null

    interface OnServiceAction {
        fun onBackUpSession(session: Session)
    }

    companion object {
        private var instance: ServiceActions? = null

        @JvmStatic
        fun getInstance(): ServiceActions {
            if (instance == null) {
                instance = ServiceActions()
            }
            return instance!!
        }
    }
}