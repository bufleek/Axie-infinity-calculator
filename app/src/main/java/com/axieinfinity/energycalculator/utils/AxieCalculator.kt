package com.axieinfinity.energycalculator.utils

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener

class AxieCalculator: Application() {
    val adUnitId = "ca-app-pub-3940256099942544/5224354917"
    val dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    var rewardsEarned = 0
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
        }

    val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }
}