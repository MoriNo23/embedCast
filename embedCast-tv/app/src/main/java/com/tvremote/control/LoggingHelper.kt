package com.embedcast.tv

import android.util.Log
import org.json.JSONObject

/**
 * Centralized logging utility that combines Android Log with WebSocket logging.
 * 
 * This class provides:
 * - Proper Android Log levels (d, i, w, e)
 * - WebSocket logging for remote debugging
 * - Backward compatibility with existing sendLog patterns
 */
object LoggingHelper {
    
    private const val TAG = "EmbedCast"
    private const val TAG_WS = "WebSocket"
    private const val TAG_PLAYER = "Player"
    private const val TAG_NETWORK = "Network"
    
    /**
     * WebSocket server reference for remote logging
     */
    private var webSocketServer: TvWebSocketServer? = null
    
    /**
     * Set the WebSocket server for remote logging
     */
    fun setWebSocketServer(server: TvWebSocketServer?) {
        webSocketServer = server
    }
    
    /**
     * Debug log - for detailed troubleshooting
     */
    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
        sendToWebSocket("d", tag, message)
    }
    
    /**
     * Info log - for general information
     */
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
        sendToWebSocket("i", tag, message)
    }
    
    /**
     * Warning log - for non-critical issues
     */
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
        sendToWebSocket("w", tag, message)
    }
    
    /**
     * Error log - for critical issues and exceptions
     */
    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        sendToWebSocket("e", tag, message)
    }
    
    // Convenience methods for specific components
    
    fun dWebSocket(message: String) = d(message, TAG_WS)
    fun iWebSocket(message: String) = i(message, TAG_WS)
    fun wWebSocket(message: String) = w(message, TAG_WS)
    fun eWebSocket(message: String, throwable: Throwable? = null) = e(message, TAG_WS, throwable)
    
    fun dPlayer(message: String) = d(message, TAG_PLAYER)
    fun iPlayer(message: String) = i(message, TAG_PLAYER)
    fun wPlayer(message: String) = w(message, TAG_PLAYER)
    fun ePlayer(message: String, throwable: Throwable? = null) = e(message, TAG_PLAYER, throwable)
    
    fun dNetwork(message: String) = d(message, TAG_NETWORK)
    fun iNetwork(message: String) = i(message, TAG_NETWORK)
    fun wNetwork(message: String) = w(message, TAG_NETWORK)
    fun eNetwork(message: String, throwable: Throwable? = null) = e(message, TAG_NETWORK, throwable)
    
    /**
     * Legacy method - maintains backward compatibility with sendLog
     */
    fun sendLog(message: String) {
        i(message, TAG)
    }
    
    /**
     * Log WebSocket connection events
     */
    fun logConnectionEvent(event: String, address: String?) {
        iWebSocket("Connection event: $event, address: $address")
    }
    
    /**
     * Log WebSocket errors with exception details
     */
    fun logWebSocketError(message: String, throwable: Throwable) {
        eWebSocket(message, throwable)
    }
    
    /**
     * Log player errors with context
     */
    fun logPlayerError(action: String, error: String, throwable: Throwable? = null) {
        ePlayer("Action '$action' failed: $error", throwable)
    }
    
    /**
     * Log network errors with URL context
     */
    fun logNetworkError(operation: String, url: String?, error: String, throwable: Throwable? = null) {
        eNetwork("Operation '$operation' failed for URL '$url': $error", throwable)
    }
    
    private fun sendToWebSocket(level: String, tag: String, message: String) {
        try {
            val logData = JSONObject().apply {
                put("type", "log")
                put("level", level)
                put("tag", tag)
                put("message", message)
                put("timestamp", System.currentTimeMillis())
            }
            webSocketServer?.sendStatus(logData)
        } catch (e: Exception) {
            // Silently fail to prevent logging loops
        }
    }
}
