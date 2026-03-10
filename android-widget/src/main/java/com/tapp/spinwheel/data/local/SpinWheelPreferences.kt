package com.tapp.spinwheel.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Simple wrapper around SharedPreferences used to keep track of when
 * the config was last refreshed. Keeping this isolated makes it easy
 * to change the persistence strategy without touching the repository.
 */
internal class SpinWheelPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastConfigFetchTimeMs(): Long = prefs.getLong(KEY_LAST_FETCH_TIME, 0L)

    fun setLastConfigFetchTimeMs(value: Long) {
        prefs.edit { putLong(KEY_LAST_FETCH_TIME, value) }
    }

    companion object {
        private const val PREFS_NAME = "spin_wheel_prefs"
        private const val KEY_LAST_FETCH_TIME = "config_last_fetch_time_ms"
    }
}

