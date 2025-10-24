package com.mybrain.playlistmaker.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getBoolean(key: String, def: Boolean = false): Boolean =
        prefs.getBoolean(key, def)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun getString(key: String, def: String? = null): String? =
        prefs.getString(key, def)

    fun putString(key: String, value: String) =
        prefs.edit { putString(key, value) }

    fun remove(key: String) =
        prefs.edit { remove(key) }
}
