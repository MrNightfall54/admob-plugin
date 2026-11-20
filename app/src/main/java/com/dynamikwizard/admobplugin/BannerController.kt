package com.dynamikwizard.admobplugin

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

internal class BannerController(
    private val onLoaded: () -> Unit,
    private val onFailed: (String) -> Unit
) {
    private var adView: AdView? = null
    private var generation = 0

    fun show(activity: Activity, adUnitId: String) {
        adView?.let {
            it.visibility = View.VISIBLE
            return
        }
        val requestGeneration = ++generation
        val banner = AdView(activity)
        adView = banner

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL }
        activity.addContentView(banner, params)

        try {
            val request = BannerAdRequest.Builder(adUnitId, AdSize.BANNER).build()
            banner.loadAd(request, object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    if (generation == requestGeneration && adView === banner) onLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (generation == requestGeneration && adView === banner) onFailed(adError.message)
                }
            })
        } catch (e: Exception) {
            destroy()
            onFailed(e.message ?: "Error al cargar banner")
        }
    }

    fun hide() {
        adView?.visibility = View.GONE
    }

    fun destroy() {
        generation++
        val banner = adView ?: return
        adView = null
        (banner.parent as? ViewGroup)?.removeView(banner)
        banner.destroy()
    }
}