package com.axieinfinity.calculator.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.intPreferencesKey
import com.axieinfinity.calculator.utils.FloatingService.Companion.INTENT_COMMAND_FLOAT
import com.axieinfinity.calculator.R
import com.axieinfinity.calculator.utils.drawOverOtherAppsEnabled
import com.axieinfinity.calculator.startFloatingService
import com.axieinfinity.calculator.utils.AxieCalculator
import com.axieinfinity.calculator.utils.FloatingWindowActions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var themeColor: Int? = null
    val dataStore by lazy { (application as AxieCalculator).dataStore }
    private var rewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        loadRewardedAd()
        if (!drawOverOtherAppsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermission()
            }
        } else {
            startFloatingService(
                themeColor = themeColor ?: ContextCompat.getColor(
                    this@MainActivity,
                    R.color.overlay
                )
            )
//            finish()
        }

        val colorPrefKey = intPreferencesKey("color")
        val colorFlow: Flow<Int> = dataStore.data.map {
            it[colorPrefKey] ?: ContextCompat.getColor(this@MainActivity, R.color.overlay)
        }

        CoroutineScope(Dispatchers.Main).launch {
            colorFlow.collectLatest {
                themeColor = it
                FloatingWindowActions.getInstance().listener?.onColorChanged(it)
            }
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdLoaded(p0: RewardedAd) {
                super.onAdLoaded(p0)
                rewardedAd = p0
                showRewardedAd()
            }
        })
    }

    private fun showRewardedAd() {
        rewardedAd?.let {
            it.show(this) {rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (drawOverOtherAppsEnabled()) {
                startFloatingService(
                    INTENT_COMMAND_FLOAT,
                    themeColor ?: ContextCompat.getColor(this@MainActivity, R.color.overlay)
                )
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted. The app will not function properly",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
//        finish()
    }

    private fun showDialog(titleText: String, messageText: String) {
        with(AlertDialog.Builder(this)) {
            title = titleText
            setMessage(messageText)
            setPositiveButton(R.string.common_ok) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        with(AlertDialog.Builder(this)) {
            title = "Permission required"
            setMessage("Please grant permission to draw over other apps")
            setPositiveButton("Allow") { dialog, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                try {
                    startActivityForResult(intent, PERMISSION_REQUEST_CODE)
                } catch (e: Exception) {
                    showDialog(
                        getString(R.string.permission_error_title),
                        getString(R.string.permission_error_text)
                    )
                }
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}