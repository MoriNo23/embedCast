package com.embedcast.tv

import android.util.Log
import android.webkit.WebView
import android.webkit.CookieManager
import android.webkit.WebStorage
import org.json.JSONObject

class VideoLoadManager(private val webView: WebView?) {

    companion object {
        private const val TAG = "VideoLoadManager"
    }
    
    private var currentUrl: String? = null
    private var lastKnownPosition: Float = 0f

    fun getCurrentUrl(): String? = currentUrl

    fun setLastPosition(position: Float) {
        lastKnownPosition = position
    }

    fun getLastPosition(): Float = lastKnownPosition

    fun loadVideo(url: String, onLoadComplete: (() -> Unit)? = null) {
        if (currentUrl != url) {
            lastKnownPosition = 0f
        }
        currentUrl = url
        Log.i(TAG, "Loading video: $url")
        webView?.loadUrl(url)
        onLoadComplete?.invoke()
    }

    fun forceReload(onComplete: (() -> Unit)? = null) {
        val url = currentUrl
        if (webView != null && url != null && url.isNotEmpty()) {
            Log.i(TAG, "Force reloading: $url")
            webView.clearCache(true)
            webView.loadUrl(url)
            onComplete?.invoke()
        } else {
            Log.w(TAG, "Cannot force reload - no URL or WebView")
        }
    }

    fun reset() {
        webView?.let { wv ->
            Log.i(TAG, "Resetting VideoLoadManager")
            wv.stopLoading()
            wv.clearCache(true)
            wv.clearHistory()
            wv.loadUrl("about:blank")
        }
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        currentUrl = null
        lastKnownPosition = 0f
    }

    fun syncState(current: Float, duration: Float, paused: Boolean, wsServer: TvWebSocketServer?) {
        val status = JSONObject().apply {
            put("type", "status")
            put("currentTime", current)
            put("duration", duration)
            put("paused", paused)
        }
        wsServer?.sendStatus(status)
    }

    fun sendLog(message: String, wsServer: TvWebSocketServer?) {
        val logData = JSONObject().apply {
            put("type", "log")
            put("message", message)
        }
        Log.i(TAG, message)
        wsServer?.sendStatus(logData)
    }
}