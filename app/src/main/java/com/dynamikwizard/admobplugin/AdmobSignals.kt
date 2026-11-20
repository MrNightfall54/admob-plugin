package com.dynamikwizard.admobplugin

import org.godotengine.godot.plugin.SignalInfo

internal object AdmobSignals {

    const val INITIALIZED = "initialized"
    const val INITIALIZATION_FAILED = "initialization_failed"

    const val BANNER_LOADED = "banner_loaded"
    const val BANNER_FAILED = "banner_failed"

    const val INTERSTITIAL_LOADED = "interstitial_loaded"
    const val INTERSTITIAL_FAILED = "interstitial_failed"
    const val INTERSTITIAL_SHOWN = "interstitial_shown"
    const val INTERSTITIAL_DISMISSED = "interstitial_dismissed"
    const val INTERSTITIAL_SHOW_FAILED = "interstitial_show_failed"

    const val REWARDED_LOADED = "rewarded_loaded"
    const val REWARDED_FAILED = "rewarded_failed"
    const val REWARDED_SHOWN = "rewarded_shown"
    const val REWARDED_DISMISSED = "rewarded_dismissed"
    const val REWARDED_SHOW_FAILED = "rewarded_show_failed"
    const val REWARDED_EARNED = "rewarded_earned"

    fun getSignals(): Set<SignalInfo> = setOf(
        SignalInfo(INITIALIZED),
        SignalInfo(INITIALIZATION_FAILED, String::class.java),
        SignalInfo(BANNER_LOADED),
        SignalInfo(BANNER_FAILED, String::class.java),
        SignalInfo(INTERSTITIAL_LOADED),
        SignalInfo(INTERSTITIAL_FAILED, String::class.java),
        SignalInfo(INTERSTITIAL_SHOWN),
        SignalInfo(INTERSTITIAL_DISMISSED),
        SignalInfo(INTERSTITIAL_SHOW_FAILED, String::class.java),
        SignalInfo(REWARDED_LOADED),
        SignalInfo(REWARDED_FAILED, String::class.java),
        SignalInfo(REWARDED_SHOWN),
        SignalInfo(REWARDED_DISMISSED),
        SignalInfo(REWARDED_SHOW_FAILED, String::class.java),
        SignalInfo(REWARDED_EARNED, String::class.java, Int::class.javaObjectType)
    )
}