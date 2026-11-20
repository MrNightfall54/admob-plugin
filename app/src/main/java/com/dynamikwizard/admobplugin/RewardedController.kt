package com.dynamikwizard.admobplugin

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback

internal class RewardedController(
    private val onLoaded: () -> Unit,
    private val onFailed: (String) -> Unit,
    private val onShown: () -> Unit,
    private val onDismissed: () -> Unit,
    private val onShowFailed: (String) -> Unit,
    private val onEarned: (String, Int) -> Unit
) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var generation = 0

    fun isReady(): Boolean = rewardedAd != null

    fun load(adUnitId: String) {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        val requestGeneration = ++generation

        val request = AdRequest.Builder(adUnitId).build()

        RewardedAd.load(
            request,
            object : AdLoadCallback<RewardedAd> {

                override fun onAdLoaded(ad: RewardedAd) {
                    if (requestGeneration != generation) return

                    isLoading = false
                    rewardedAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(
                    adError: LoadAdError
                ) {
                    if (requestGeneration != generation) return

                    isLoading = false
                    rewardedAd = null
                    onFailed(adError.message)
                }
            }
        )
    }

    fun show(activity: Activity) {
        val ad = rewardedAd ?: run {
            onShowFailed("Rewarded ad is not ready")
            return
        }

        rewardedAd = null

        ad.adEventCallback =
            object : RewardedAdEventCallback {

                override fun onAdShowedFullScreenContent() {
                    onShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    onShowFailed(fullScreenContentError.message)
                }
            }

        ad.show(
            activity
        ) { rewardItem ->
            onEarned(
                rewardItem.type,
                rewardItem.amount
            )
        }
    }

    fun clear() {
        generation++
        rewardedAd = null
        isLoading = false
    }
}