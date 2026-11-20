package com.dynamikwizard.admobplugin

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class AdmobPlugin(godot: Godot) : GodotPlugin(godot) {
    private val tag = BuildConfig.LOG_NAME

    @Volatile
    private var currentActivity: Activity? = null
    @Volatile
    private var initialized = false
    @Volatile
    private var initializing = false

    private val banner = BannerController(
        onLoaded = { sendSignal(AdmobSignals.BANNER_LOADED) },
        onFailed = { sendSignal(AdmobSignals.BANNER_FAILED, it) }
    )
    private val interstitial = InterstitialController(
        onLoaded = { sendSignal(AdmobSignals.INTERSTITIAL_LOADED) },
        onFailed = { sendSignal(AdmobSignals.INTERSTITIAL_FAILED, it) },
        onShown = { sendSignal(AdmobSignals.INTERSTITIAL_SHOWN) },
        onDismissed = { sendSignal(AdmobSignals.INTERSTITIAL_DISMISSED) },
        onShowFailed = { sendSignal(AdmobSignals.INTERSTITIAL_SHOW_FAILED, it) }
    )
    private val rewarded = RewardedController(
        onLoaded = { sendSignal(AdmobSignals.REWARDED_LOADED) },
        onFailed = { sendSignal(AdmobSignals.REWARDED_FAILED, it) },
        onShown = { sendSignal(AdmobSignals.REWARDED_SHOWN) },
        onDismissed = { sendSignal(AdmobSignals.REWARDED_DISMISSED) },
        onShowFailed = { sendSignal(AdmobSignals.REWARDED_SHOW_FAILED, it) },
        onEarned = { type, amount -> sendSignal(AdmobSignals.REWARDED_EARNED, type, amount) }
    )

    override fun getPluginName(): String = BuildConfig.PLUGIN_NAME
    override fun getPluginSignals(): Set<SignalInfo> = AdmobSignals.getSignals()

    override fun onMainCreate(activity: Activity?): View? {
        currentActivity = activity
        Log.d(tag, "onMainCreate -> AdMob Next-Gen")
        return super.onMainCreate(activity)
    }

    private fun sendSignal(name: String, vararg args: Any) {
        currentActivity?.runOnUiThread { emitSignal(name, *args) }
    }

    @UsedByGodot
    fun initialize(applicationId: String) {

        if (applicationId.isEmpty()){
            sendSignal(AdmobSignals.INITIALIZATION_FAILED, "Empty applicationId")
            return
        }

        val host = currentActivity ?: run {
            sendSignal(AdmobSignals.INITIALIZATION_FAILED, "Activity unavailable")
            return
        }
        synchronized(this) {
            if (initialized) {
                sendSignal(AdmobSignals.INITIALIZED)
                return
            }
            if (initializing) return
            initializing = true
        }

        Thread {
            try {
                val config = InitializationConfig.Builder(applicationId = applicationId).build()
                MobileAds.initialize(host.applicationContext, config) { _ ->
                    synchronized(this) {
                        initialized = true
                        initializing = false
                    }
                    sendSignal(AdmobSignals.INITIALIZED)
                }
            } catch (e: Exception) {
                synchronized(this) { initializing = false }
                Log.e(tag, "Failed to initialize GMA Next-Gen", e)
                sendSignal(AdmobSignals.INITIALIZATION_FAILED, e.message ?: "Initialization error")
            }
        }.start()
    }

    @UsedByGodot
    fun is_initialized(): Boolean = initialized

    @UsedByGodot
    fun show_banner(adUnitId: String) {
        if (adUnitId.isEmpty()){
            sendSignal(AdmobSignals.BANNER_FAILED, "Empty banner id")
            return
        }

        if (!initialized) {
            sendSignal(AdmobSignals.BANNER_FAILED, "AdMob is not initialized")
            return
        }
        val host = currentActivity ?: run {
            sendSignal(AdmobSignals.BANNER_FAILED, "Activity unavailable")
            return
        }
        host.runOnUiThread { banner.show(host, adUnitId) }
    }

    @UsedByGodot
    fun hide_banner() {
        currentActivity?.runOnUiThread { banner.hide() }
    }

    @UsedByGodot
    fun destroy_banner() {
        currentActivity?.runOnUiThread { banner.destroy() }
    }

    @UsedByGodot
    fun load_interstitial(adUnitId: String) {
        if (adUnitId.isEmpty()){
            sendSignal(AdmobSignals.INTERSTITIAL_FAILED, "Empty interstitial id")
            return
        }

        if (!initialized) {
            sendSignal(AdmobSignals.INTERSTITIAL_FAILED, "AdMob is not initialized")
            return
        }
        val host = currentActivity ?: run {
            sendSignal(AdmobSignals.INTERSTITIAL_FAILED, "Activity unavailable")
            return
        }
        host.runOnUiThread { interstitial.load(adUnitId) }
    }

    @UsedByGodot
    fun show_interstitial() {
        val host = currentActivity ?: return
        host.runOnUiThread { interstitial.show(host) }
    }

    @UsedByGodot
    fun is_interstitial_ready(): Boolean = interstitial.isReady()

    @UsedByGodot
    fun load_rewarded(adUnitId: String) {
        if (adUnitId.isEmpty()){
            sendSignal(AdmobSignals.REWARDED_FAILED, "Empty rewarded id")
            return
        }

        if (!initialized) {
            sendSignal(AdmobSignals.REWARDED_FAILED, "AdMob is not initialized")
            return
        }
        val host = currentActivity ?: run {
            sendSignal(AdmobSignals.REWARDED_FAILED, "Activity unavailable")
            return
        }
        host.runOnUiThread { rewarded.load(adUnitId) }
    }

    @UsedByGodot
    fun show_rewarded() {
        val host = currentActivity ?: return
        host.runOnUiThread { rewarded.show(host) }
    }

    @UsedByGodot
    fun is_rewarded_ready(): Boolean = rewarded.isReady()

    @UsedByGodot
    fun clear_ads() {
        currentActivity?.runOnUiThread {
            banner.destroy()
            interstitial.clear()
            rewarded.clear()
        }
    }
}