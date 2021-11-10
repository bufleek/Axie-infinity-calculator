package com.axiearena.energycalculator.utils

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class AxieCalculator : Application() {
    val dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    var rewardsEarned = 0
}