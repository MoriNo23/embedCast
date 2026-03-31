package com.embedcast.tv

import android.util.Log
import org.json.JSONObject
import java.net.InetAddress
import java.net.UnknownHostException

class WebSocketManager(private val port: Int = DEFAULT_PORT) {

    private var wsServer: TvWebSocketServer? = null
    private var onCommandCallback: ((JSONObject) -> Unit)? = null
    
    companion object {
        private const val TAG = "WebSocketManager"
        private const val DEFAULT_PORT = 8080
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }
    
    private var retryCount = 0
    private var isRetrying = false

    fun startServer(onCommandReceived: (JSONObject) -> Unit) {
        onCommandCallback = onCommandReceived
        wsServer = TvWebSocketServer(port) { json ->
            onCommandCallback?.invoke(json)
        }
        wsServer?.isReuseAddr = true
        LoggingHelper.setWebSocketServer(wsServer)
        startServerInternal()
    }
    
    private fun startServerInternal() {
        try {
            wsServer?.start()
            retryCount = 0
            isRetrying = false
            Log.i(TAG, "WebSocket server started on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start WebSocket server: ${e.message}", e)
            handleStartError(e)
        }
    }
    
    private fun handleStartError(e: Exception) {
        val message = when {
            e is UnknownHostException -> "Unknown host error"
            e.message?.contains("Address already in use") == true -> "Port $port already in use"
            e.message?.contains("Permission denied") == true -> "Permission denied - check network permissions"
            else -> "Failed to start server: ${e.message}"
        }
        Log.e(TAG, message, e)
        
        if (!isRetrying && retryCount < MAX_RETRY_ATTEMPTS) {
            isRetrying = true
            retryCount++
            Log.w(TAG, "Retrying server start ($retryCount/$MAX_RETRY_ATTEMPTS) in $RETRY_DELAY_MS ms")
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isRetrying = false
                startServerInternal()
            }, RETRY_DELAY_MS)
        } else if (retryCount >= MAX_RETRY_ATTEMPTS) {
            Log.e(TAG, "Max retry attempts reached. Server failed to start.")
        }
    }

    fun stopServer() {
        try {
            wsServer?.stop()
            Log.i(TAG, "WebSocket server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping WebSocket server: ${e.message}", e)
        }
        wsServer = null
        LoggingHelper.setWebSocketServer(null)
    }

    fun sendStatus(status: JSONObject) {
        wsServer?.sendStatus(status)
    }

    fun sendLog(message: String) {
        val logData = JSONObject().apply {
            put("type", "log")
            put("message", message)
        }
        wsServer?.sendStatus(logData)
    }

    fun isRunning(): Boolean = wsServer != null
    
    fun getRetryCount(): Int = retryCount
}