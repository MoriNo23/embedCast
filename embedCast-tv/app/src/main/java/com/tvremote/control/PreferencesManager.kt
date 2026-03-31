package com.embedcast.tv

import android.content.SharedPreferences

class PreferencesManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val PREFIX_PROGRESS = "progress_"
    }

    fun saveProgress(url: String, timeMs: Float) {
        val safeKey = sanitizeUrl(url)
        sharedPreferences.edit()
            .putFloat("$PREFIX_PROGRESS$safeKey", timeMs)
            .apply()
    }

    fun loadProgress(url: String): Float {
        val safeKey = sanitizeUrl(url)
        return sharedPreferences.getFloat("$PREFIX_PROGRESS$safeKey", 0f)
    }

    fun clearProgress(url: String) {
        val safeKey = sanitizeUrl(url)
        sharedPreferences.edit()
            .remove("$PREFIX_PROGRESS$safeKey")
            .apply()
    }

    fun clearAllProgress() {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys
            .filter { it.startsWith(PREFIX_PROGRESS) }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    private fun sanitizeUrl(url: String): String {
        return url.replace("[^a-zA-Z0-9]".toRegex(), "_")
    }
}