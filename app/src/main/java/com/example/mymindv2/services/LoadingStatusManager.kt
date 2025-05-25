package com.example.mymindv2.services

import android.content.Context
import android.content.SharedPreferences

object LoadingStatusManager {

    private const val PREFS_NAME = "loading_prefs"
    private const val KEY_IS_LOADING = "is_loading"

    fun setLoading(context: Context, isLoading: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOADING, isLoading).apply()
    }

    fun isLoading(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOADING, false)
    }
}
