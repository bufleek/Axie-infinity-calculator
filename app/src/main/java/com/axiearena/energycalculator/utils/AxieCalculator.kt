package com.axiearena.energycalculator.utils

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore


val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(name = "backup")
class AxieCalculator : Application() {
    val dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    var rewardsEarned = 0
}