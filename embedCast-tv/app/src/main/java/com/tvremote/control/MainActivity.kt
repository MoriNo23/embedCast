package com.embedcast.tv

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import android.media.session.MediaSession
import android.media.session.MediaSession.Callback
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.graphics.Color
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), VideoWebViewClient.ErrorListener {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val UPDATE_SERVER_URL = "http://10.42.0.1:8000"
        private const val DEFAULT_WEBSOCKET_PORT = 8080
        private const val SAVE_INTERVAL_MS = 30_000L
        private const val QUALITY_BUTTON_TEXT_COLOR = Color.WHITE
        private const val QUALITY_BUTTON_BACKGROUND_COLOR = 0xFF404040.toInt()
        private const val CONNECTION_TIMEOUT_MS = 3000
        private const val READ_TIMEOUT_MS = 3000
        private const val UI_HIDE_DELAY_MS = 3000L
        private const val STATUS_MESSAGE_DELAY_MS = 1500L
        private const val UPDATE_PROMPT_DELAY_MS = 5000L
        private const val QUALITY_EXTRACTION_DELAY_MS = 2000L
        private const val RECONNECT_DELAY_MS = 2000L
        private const val AUTO_RECOVER_DELAY_MS = 3000L
    }
    
    private val qualityMenuContainer: LinearLayout? by lazy { findViewById(R.id.qualityMenuContainer) }
    private val qualityListContainer: LinearLayout? by lazy { findViewById(R.id.qualityListContainer) }

    private lateinit var webSocketManager: WebSocketManager
    private lateinit var preferencesManager: PreferencesManager
    private var videoPlayerManager: VideoPlayerManager? = null
    private var videoLoadManager: VideoLoadManager? = null

    private var webView: WebView? = null
    private lateinit var guideWebView: WebView
    private lateinit var webViewContainer: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var playerOverlay: LinearLayout
    private lateinit var videoSeekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var qualityText: TextView

    private val hideOverlayRunnable = Runnable { hideOverlay() }
    private var pendingSeekTimeSec: Float = 0f
    private var currentDuration: Float = 0f
    private var mediaSession: MediaSession? = null
    private var overlayVisible = false
    private var lastSaveTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webViewContainer = findViewById(R.id.webViewContainer)
        guideWebView = findViewById(R.id.guideWebView)
        statusText = findViewById(R.id.statusText)
        playerOverlay = findViewById(R.id.playerOverlay)
        videoSeekBar = findViewById(R.id.videoSeekBar)
        currentTimeText = findViewById(R.id.currentTimeText)
        durationText = findViewById(R.id.durationText)
        qualityText = findViewById(R.id.qualityText)

        preferencesManager = PreferencesManager(getSharedPreferences("embedcast_prefs", Context.MODE_PRIVATE))
        webSocketManager = WebSocketManager(8080)

        WebView.setWebContentsDebuggingEnabled(true)
        guideWebView.settings.apply {
            javaScriptEnabled = true; domStorageEnabled = true; loadWithOverviewMode = true; useWideViewPort = true
        }
        guideWebView.loadUrl("file:///android_asset/remote_guide.html")
        guideWebView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                guideWebView.evaluateJavascript("setTvIp('ws://${getLocalIpAddress() ?: "Unknown IP"}:8080')", null)
            }
        }

        videoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    showOverlay()
                    if (currentDuration > 0) {
                        pendingSeekTimeSec = (progress / 100f) * currentDuration
                        currentTimeText.text = formatTime(pendingSeekTimeSec)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { showOverlay() }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (pendingSeekTimeSec > 0 && webView != null) {
                    webView?.evaluateJavascript(seekScript(pendingSeekTimeSec), null)
                }
                statusText.removeCallbacks(hideOverlayRunnable)
                statusText.postDelayed(hideOverlayRunnable, 3000)
            }
        })

        mediaSession = MediaSession(this, "EmbedCast Session").apply {
            setCallback(object : Callback() {
                override fun onPlay() { runOnUiThread { controlVideo("play") } }
                override fun onPause() { runOnUiThread { controlVideo("pause") } }
                override fun onSeekTo(pos: Long) { runOnUiThread { controlVideo("seek", (pos/1000).toString()) } }
                override fun onStop() { runOnUiThread { resetUI(); videoLoadManager?.setLastPosition(0f); showIdleStatus() } }
                override fun onRewind() { runOnUiThread { controlVideo("seek", "-10") } }
                override fun onFastForward() { runOnUiThread { controlVideo("seek", "10") } }
            })
            isActive = true
        }

        startServer()
        checkForUpdates()
    }

    private fun checkForUpdates() {
        Thread {
            try {
                val conn = URL("$UPDATE_SERVER_URL/version.json").openConnection() as HttpURLConnection
                conn.connectTimeout = 3000; conn.readTimeout = 3000
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val serverVer = JSONObject(InputStreamReader(conn.inputStream).readText()).getInt("versionCode")
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val currentVer = PackageInfoCompat.getLongVersionCode(packageInfo)
                    if (serverVer > currentVer) runOnUiThread { promptUpdate() }
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Update check failed: ${e.message}")
                webSocketManager.sendLog("Error buscando actualizaciones: ${e.message}")
            }
        }.start()
    }

    private fun promptUpdate() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$UPDATE_SERVER_URL/app-debug.apk")))
            statusText.text = "Actualizacion disponible!\nDescargando..."
            statusText.visibility = View.VISIBLE
            statusText.postDelayed({ statusText.visibility = View.GONE }, 5000)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open update URL: ${e.message}", e)
            webSocketManager.sendLog("Error al actualizar: ${e.message}")
        }
    }

    private fun createNewWebView(): WebView {
        webViewContainer.removeAllViews()
        webView?.destroy()
        val newWebView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            isFocusable = true; isFocusableInTouchMode = true
        }
        newWebView.settings.apply {
            javaScriptEnabled = true; domStorageEnabled = true; databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW; allowFileAccess = true
            setSupportMultipleWindows(false); javaScriptCanOpenWindowsAutomatically = false
            setNeedInitialFocus(true)
        }
        newWebView.webViewClient = object : VideoWebViewClient(this) {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.postDelayed({ extractQualities() }, 2000)
            }
        }
        newWebView.webChromeClient = VideoChromeClient()
        newWebView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onVideoUpdate(currentTime: Float, duration: Float, paused: Boolean) {
                runOnUiThread {
                    var safe = currentTime; if (safe.isNaN()) safe = 0f
                    videoLoadManager?.setLastPosition(safe)
                    syncStateToLaptop(safe, duration, paused)
                    if (duration > 0) {
                        videoSeekBar.progress = ((safe / duration) * 100).toInt()
                        currentTimeText.text = formatTime(safe)
                        durationText.text = formatTime(duration)
                        if (!overlayVisible) showOverlay()
                    }
                    val now = System.currentTimeMillis()
                    if (now - lastSaveTime >= SAVE_INTERVAL_MS) { saveCurrentProgress(); lastSaveTime = now }
                }
            }
            @JavascriptInterface
            fun onQualitiesFound(qualities: String) {
                runOnUiThread {
                    try {
                        val arr = org.json.JSONArray(qualities)
                        val container = qualityListContainer ?: return@runOnUiThread
                        container.removeAllViews()
                        if (arr.length() == 0) {
                            container.addView(makeQualityButton("Calidad Unica (Auto)") { closeQualityMenu() })
                            return@runOnUiThread
                        }
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            container.addView(makeQualityButton(item.getString("label")) {
                                webView?.evaluateJavascript("jwplayer().setCurrentQuality(${item.getInt("id")})", null)
                                closeQualityMenu()
                            })
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing qualities: ${e.message}")
                        webSocketManager.sendLog("Error parsing qualities: ${e.message}")
                    }
                }
            }
            @JavascriptInterface
            fun onQualityUpdated(quality: String) {
                runOnUiThread {
                    qualityText.text = quality; qualityText.visibility = View.VISIBLE
                    qualityText.postDelayed({ qualityText.visibility = View.GONE }, 3000)
                }
            }
            @JavascriptInterface
            fun onConnectionInterrupted() = this@MainActivity.onConnectionInterrupted()
            @JavascriptInterface
            fun getResumeTime(): Float = videoLoadManager?.getLastPosition() ?: 0f
            @JavascriptInterface
            fun showToast(msg: String) {
                runOnUiThread {
                    statusText.text = msg; statusText.visibility = View.VISIBLE
                    statusText.postDelayed({ statusText.visibility = View.GONE }, 3000)
                }
            }
        }, "AndroidTV")
        webViewContainer.addView(newWebView)
        videoPlayerManager = VideoPlayerManager(newWebView)
        videoLoadManager = VideoLoadManager(newWebView)
        return newWebView
    }

    private fun makeQualityButton(label: String, onClick: () -> Unit) =
        android.widget.Button(this).apply {
            text = label; textSize = 16f; setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#404040"))
            isFocusable = true; isFocusableInTouchMode = true; setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(0, 0, 0, 10) }
            setOnClickListener { onClick() }
        }

    private fun extractQualities() {
        webView?.evaluateJavascript("""
            (function() {
                try {
                    if (typeof window.jwplayer !== 'undefined' && typeof jwplayer().getQualityLevels === 'function') {
                        var levels = jwplayer().getQualityLevels();
                        if (levels && levels.length > 0) {
                            var simple = [];
                            for (var i = 0; i < levels.length; i++) {
                                simple.push({id: i, label: levels[i].label || 'Level ' + i});
                            }
                            AndroidTV.onQualitiesFound(JSON.stringify(simple));
                        }
                    }
                    if (typeof jwplayer !== 'undefined') {
                        jwplayer().on('error', function(e) { if(typeof AndroidTV !== 'undefined') AndroidTV.onConnectionInterrupted(); });
                        jwplayer().on('setupError', function(e) { if(typeof AndroidTV !== 'undefined') AndroidTV.onConnectionInterrupted(); });
                    }
                } catch(e) {}
            })();
        """.trimIndent(), null)
    }

    private fun startServer() {
        statusText.text = "EmbedCast\nServer: ws://${getLocalIpAddress() ?: "Unknown IP"}:8080"
        webSocketManager.startServer { json -> runOnUiThread { handleCommand(json) } }
    }

    private fun handleCommand(json: JSONObject) {
        when (json.optString("action")) {
            "load" -> { val url = json.optString("url"); if (url.isNotEmpty()) loadVideoUrl(url) }
            "play" -> controlVideo("play")
            "pause" -> controlVideo("pause")
            "stop" -> { resetUI(); videoLoadManager?.setLastPosition(0f); showIdleStatus() }
            "seek" -> controlVideo("seek", json.optInt("seconds", 0).toString())
            "quality" -> controlVideo("quality", json.optString("value", "1"))
            "reload" -> forceReload()
        }
    }

    private fun loadVideoUrl(url: String) {
        val savedPos = preferencesManager.loadProgress(url)
        runOnUiThread {
            webSocketManager.sendLog("Loading URL: $url")
            statusText.text = "Loading..."; statusText.visibility = View.VISIBLE
            statusText.postDelayed({ statusText.visibility = View.GONE }, 1500)
            guideWebView.visibility = View.GONE; webViewContainer.visibility = View.VISIBLE
            if (webView == null) webView = createNewWebView()
            videoLoadManager?.loadVideo(url); videoLoadManager?.setLastPosition(savedPos)
            webView?.requestFocus()
        }
    }

    private fun forceReload() {
        runOnUiThread {
            val url = videoLoadManager?.getCurrentUrl()
            if (webView != null && url != null && url.isNotEmpty()) {
                webSocketManager.sendLog("Hard Reloading player (Clearing cache for new token)...")
                statusText.text = "Regenerando Token..."; statusText.visibility = View.VISIBLE
                statusText.postDelayed({ statusText.visibility = View.GONE }, 1500)
                webView?.clearCache(true); webView?.loadUrl(url); webView?.requestFocus()
            } else { webSocketManager.sendLog("No active player or URL to reload") }
        }
    }

    private fun resetUI() {
        webViewContainer.removeAllViews()
        videoLoadManager?.reset(); webView?.destroy(); webView = null
        playerOverlay.visibility = View.GONE; videoSeekBar.progress = 0
        currentTimeText.text = "00:00"; durationText.text = "00:00"; qualityText.text = "Quality: Auto"
        webViewContainer.visibility = View.GONE; guideWebView.visibility = View.VISIBLE
        syncStateToLaptop(0f, 0f, true)
    }

    private fun controlVideo(action: String, value: String = "") {
        when (action) {
            "play", "pause" -> videoPlayerManager?.playPause()
            "seek" -> videoPlayerManager?.seek(value.toIntOrNull() ?: 0)
            "quality" -> videoPlayerManager?.setQuality(value.toIntOrNull() ?: 1)
            "subtitles" -> videoPlayerManager?.toggleSubtitles()
            "seekPercent" -> videoPlayerManager?.seekPercent(value.toFloatOrNull() ?: 0f)
        }
    }

    private fun openQualityMenu() {
        val container = qualityMenuContainer ?: return
        container.visibility = View.VISIBLE
        val first = qualityListContainer?.getChildAt(0)
        if (first != null) first.requestFocus() else container.requestFocus()
    }

    private fun closeQualityMenu() { qualityMenuContainer?.visibility = View.GONE; webView?.requestFocus() }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            webSocketManager.sendLog("Key Event: ${event.keyCode}")
            if (qualityMenuContainer?.visibility == View.VISIBLE) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BACK -> { closeQualityMenu(); return true }
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER ->
                        return super.dispatchKeyEvent(event)
                }
            }
            val seekPercents = mapOf(
                KeyEvent.KEYCODE_0 to "0", KeyEvent.KEYCODE_1 to "0.1", KeyEvent.KEYCODE_2 to "0.2",
                KeyEvent.KEYCODE_3 to "0.3", KeyEvent.KEYCODE_4 to "0.4", KeyEvent.KEYCODE_5 to "0.5",
                KeyEvent.KEYCODE_6 to "0.6", KeyEvent.KEYCODE_7 to "0.7", KeyEvent.KEYCODE_8 to "0.8",
                KeyEvent.KEYCODE_9 to "0.9"
            )
            seekPercents[event.keyCode]?.let { controlVideo("seekPercent", it); return true }
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> { openQualityMenu(); return true }
                KeyEvent.KEYCODE_DPAD_LEFT -> { controlVideo("seek", "-10"); return true }
                KeyEvent.KEYCODE_DPAD_RIGHT -> { controlVideo("seek", "10"); return true }
                KeyEvent.KEYCODE_INFO -> {
                    if (guideWebView.visibility == View.VISIBLE) { guideWebView.visibility = View.GONE; webView?.requestFocus() }
                    else { guideWebView.visibility = View.VISIBLE; guideWebView.requestFocus() }
                    return true
                }
                KeyEvent.KEYCODE_CAPTIONS, KeyEvent.KEYCODE_S -> { controlVideo("subtitles"); return true }
                KeyEvent.KEYCODE_PROG_RED -> { forceReload(); return true }
                KeyEvent.KEYCODE_PROG_BLUE, KeyEvent.KEYCODE_MEDIA_STOP -> {
                    if (webView != null) { resetUI(); videoLoadManager?.setLastPosition(0f); showIdleStatus(); return true }
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (webView != null) { resetUI(); videoLoadManager?.setLastPosition(0f); showIdleStatus(); return true }
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    controlVideo("play"); return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = super.onKeyDown(keyCode, event)

    override fun onError(description: String) {
        runOnUiThread {
            statusText.text = "Error: $description\nPress Menu to retry"
            statusText.visibility = View.VISIBLE; webViewContainer.visibility = View.GONE
        }
    }

    override fun onRetry(url: String) {
        runOnUiThread {
            statusText.text = "Reconnecting..."; statusText.visibility = View.VISIBLE
            statusText.postDelayed({ statusText.visibility = View.GONE }, 1500)
        }
    }

    override fun onConnectionInterrupted() {
        runOnUiThread {
            statusText.text = "Connection lost...\nAuto-recovering in 3s"
            statusText.visibility = View.VISIBLE
            statusText.postDelayed({
                forceReload()
                webView?.postDelayed({ webView?.evaluateJavascript("if(typeof jwplayer !== 'undefined') jwplayer().play(true);", null) }, 2000)
            }, 3000)
        }
    }

    override fun getResumeTime(): Float = videoLoadManager?.getLastPosition() ?: 0f

    private fun syncStateToLaptop(current: Float, duration: Float, paused: Boolean) {
        webSocketManager.sendStatus(JSONObject().apply {
            put("type", "status"); put("currentTime", current); put("duration", duration); put("paused", paused)
        })
    }

    private fun hideOverlay() { playerOverlay.visibility = View.GONE; overlayVisible = false }

    private fun showOverlay() {
        if (!overlayVisible) { playerOverlay.visibility = View.VISIBLE; overlayVisible = true }
        statusText.removeCallbacks(hideOverlayRunnable)
        statusText.postDelayed(hideOverlayRunnable, 3000)
    }

    private fun formatTime(seconds: Float): String {
        val s = Math.max(0, seconds.toInt())
        return String.format("%02d:%02d", s / 60, s % 60)
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                val addrs = ni.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) return addr.hostAddress
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get local IP address: ${e.message}", e)
        }
        return null
    }

    private fun saveCurrentProgress() {
        val url = videoLoadManager?.getCurrentUrl()
        if (url != null && url.isNotEmpty()) {
            preferencesManager.saveProgress(url, videoLoadManager?.getLastPosition() ?: 0f)
        }
    }

    private fun showIdleStatus() {
        statusText.text = "EmbedCast\nServer: ws://${getLocalIpAddress() ?: "Unknown IP"}:8080"
        statusText.visibility = View.VISIBLE
    }

    private fun seekScript(timeSec: Float): String = """
        (function() { try {
            var hasJW = typeof window.jwplayer !== 'undefined'; var v = document.querySelector('video');
            if (hasJW && typeof jwplayer().seek === 'function') jwplayer().seek($timeSec);
            else if (v) v.currentTime = $timeSec;
        } catch(e) { console.log('Error in seek: ' + e); } })();
    """.trimIndent()

    override fun onDestroy() { super.onDestroy(); webSocketManager.stopServer(); webView?.destroy() }
}
