package com.dynamikwizard.admobplugin

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

internal class InterstitialController(
    private val onLoaded: () -> Unit,
    private val onFailed: (String) -> Unit,
    private val onShown: () -> Unit,
    private val onDismissed: () -> Unit,
    private val onShowFailed: (String) -> Unit
) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var generation = 0

    fun isReady(): Boolean = interstitialAd != null

    fun load(adUnitId: String) {
        if (isLoading || interstitialAd != null) return

        isLoading = true
        val requestGeneration = ++generation

        val request = AdRequest.Builder(adUnitId).build()

        InterstitialAd.load(
            request,
            object : AdLoadCallback<InterstitialAd> {

                override fun onAdLoaded(ad: InterstitialAd) {
                    if (requestGeneration != generation) return

                    isLoading = false
                    interstitialAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(
                    adError: LoadAdError
                ) {
                    if (requestGeneration != generation) return

                    isLoading = false
                    interstitialAd = null
                    onFailed(adError.message)
                }
            }
        )
    }

    fun show(activity: Activity) {
        val ad = interstitialAd ?: run {
            onShowFailed("Interstitial is not ready")
            return
        }

        interstitialAd = null

        ad.adEventCallback =
            object : InterstitialAdEventCallback {

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

        ad.show(activity)
    }

    fun clear() {
        generation++
        interstitialAd = null
        isLoading = false
    }
}