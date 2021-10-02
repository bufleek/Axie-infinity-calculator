package com.axieinfinity.energycalculator.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.axieinfinity.energycalculator.R
import com.axieinfinity.energycalculator.startFloatingService
import com.axieinfinity.energycalculator.utils.AxieCalculator
import com.axieinfinity.energycalculator.utils.CardCounterActions
import com.axieinfinity.energycalculator.utils.FloatingService.Companion.INTENT_COMMAND_FLOAT
import com.axieinfinity.energycalculator.utils.FloatingWindowActions
import com.axieinfinity.energycalculator.utils.drawOverOtherAppsEnabled
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var btnWatchAds: Button
    private lateinit var tvRewardsEarned: TextView
    private lateinit var progressLoadingAd: ProgressBar
    private var themeColor: Int? = null
    private val dataStore by lazy { (application as AxieCalculator).dataStore }
    private var rewardedAd: RewardedAd? = null
    private var roundsExhaustedMessage: String? = null
    private lateinit var rvSubs: RecyclerView
    private val subsAdapter = SubsAdapter {
        startBilling()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnWatchAds = findViewById(R.id.btn_watch_ads)
        tvRewardsEarned = findViewById(R.id.tv_rewards)
        progressLoadingAd = findViewById(R.id.progress_ad_loading)
        rvSubs = findViewById(R.id.rv_subs)

        rvSubs.apply {
            adapter = subsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        MobileAds.initialize(this)

        roundsExhaustedMessage = intent.getStringExtra(INTENT_ROUNDS_EXHAUSTED)
        if (roundsExhaustedMessage != null) {
            (application as AxieCalculator).rewardsEarned = 0
            Snackbar.make(this, btnWatchAds, roundsExhaustedMessage!!, Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {}
                .show()
        }

        val neededRewards = REWARDS_REQUIRED - (application as AxieCalculator).rewardsEarned
        tvRewardsEarned.text = "${(application as AxieCalculator).rewardsEarned}/$REWARDS_REQUIRED"

        btnWatchAds.setOnClickListener {
            btnWatchAds.isEnabled = false
            progressLoadingAd.visibility = View.VISIBLE
            loadRewardedAd()
        }
        if (neededRewards <= 0) {
            showFloatingWindow()
        } else {
            initAd()
        }
    }

    private fun querySkuDetails() {
        val skuList = ArrayList<String>().apply {
            add("monthly_subscription")
        }
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        (application as AxieCalculator).billingClient.querySkuDetailsAsync(
            params
        ) { p0, p1 ->
            if (p1 != null) {
                billSku(p1[0])
                showSubs(p1 as ArrayList<SkuDetails>)
            }
        }

    }

    private fun startBilling() {
        subsAdapter.updateData(ArrayList(), true)
        (application as AxieCalculator).billingClient.startConnection(object :
            BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Snackbar.make(
                    this@MainActivity,
                    rvSubs,
                    "Failed to load subscriptions",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Cancel") {
                        showSubs(null)
                    }
                    .setAction("Retry") {
                        startBilling()
                    }.show()
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                querySkuDetails()
            }

        })
    }

    private fun showFloatingWindow() {
        if ((application as AxieCalculator).rewardsEarned >= REWARDS_REQUIRED) {
            if (!drawOverOtherAppsEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission()
                }
            } else {
                if (roundsExhaustedMessage != null) {
                    FloatingWindowActions.getInstance().listener?.onRoundsRefilled()
                } else {
                    startFloatingService(
                        themeColor = themeColor ?: ContextCompat.getColor(
                            this@MainActivity,
                            R.color.overlay
                        )
                    )
                }

                val colorPrefKey = intPreferencesKey("color")
                val colorFlow: Flow<Int> = dataStore.data.map {
                    it[colorPrefKey] ?: ContextCompat.getColor(this@MainActivity, R.color.overlay)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    colorFlow.collectLatest {
                        themeColor = it
                        FloatingWindowActions.getInstance().listener?.onColorChanged(it)
                        CardCounterActions.getInstance().listener?.onColorChanged(it)
                    }
                }
                finish()
            }
        }
    }

    private fun initAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            (application as AxieCalculator).adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    rewardedAd = p0
                }
            })
    }

    private fun loadRewardedAd() {
        if (rewardedAd == null) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                this,
                (application as AxieCalculator).adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        btnWatchAds.isEnabled = true
                        progressLoadingAd.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Failed to load Ad", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAdLoaded(p0: RewardedAd) {
                        super.onAdLoaded(p0)
                        rewardedAd = p0
                        btnWatchAds.isEnabled = true
                        progressLoadingAd.visibility = View.GONE
                        showRewardedAd()
                    }
                })
        } else {
            showRewardedAd()
        }
    }

    private fun showRewardedAd() {
        btnWatchAds.isEnabled = true
        progressLoadingAd.visibility = View.GONE
        rewardedAd?.let {
            it.show(this) { rewardItem ->
                rewardedAd = null
                if ((application as AxieCalculator).rewardsEarned < REWARDS_REQUIRED) {
                    (application as AxieCalculator).rewardsEarned++
                }
                if ((application as AxieCalculator).rewardsEarned < REWARDS_REQUIRED) {
                    val rewards = (application as AxieCalculator).rewardsEarned
                    tvRewardsEarned.text = "$rewards/$REWARDS_REQUIRED"
                    Toast.makeText(this, "1 Reward Earned", Toast.LENGTH_SHORT)
                        .show()
                    initAd()
                } else {
                    showFloatingWindow()
                }
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
        finish()
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

    private fun showSubs(skuDetails: List<SkuDetails>?) {
        Toast.makeText(this, "called with ${skuDetails?.size ?: 0}", Toast.LENGTH_SHORT).show()
        subsAdapter.updateData(ArrayList(skuDetails ?: emptyList()))
    }

    private fun billSku(sku: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(sku)
            .build()
        val responseCode = (application as AxieCalculator).billingClient.launchBillingFlow(
            this@MainActivity,
            flowParams
        ).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(this, "Failed to launch billing process", Toast.LENGTH_LONG)
                .show()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 0
        const val REWARDS_REQUIRED = 0
        const val INTENT_ROUNDS_EXHAUSTED = "ROUNDS_EXHAUSTED"
        const val MESSAGE_ROUNDS_EXHAUSTED =
            "Your free rounds have been exhausted, earn more rewards or unlock our paid plan"
    }
}