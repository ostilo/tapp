package com.tapp.spinwheel.core

import android.util.Log
import com.tapp.spinwheel.BuildConfig

/**
 * Centralised logger for the widget module.
 *
 * Logging is automatically disabled in release builds via BuildConfig.DEBUG.
 */
internal object SpinWheelLogger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
    }
}

