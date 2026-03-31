package com.embedcast.tv

import android.util.Log
import org.java_websocket.server.WebSocketServer
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.WebSocket
import java.net.InetSocketAddress
import org.json.JSONObject

class TvWebSocketServer(
    port: Int,
    private val onCommandReceived: (JSONObject) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    companion object {
        private const val TAG = "TvWebSocketServer"
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        val address = conn?.remoteSocketAddress?.address?.hostAddress
        Log.i(TAG, "Remote control connected: $address")
        LoggingHelper.logConnectionEvent("OPEN", address)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let {
            try {
                val json = JSONObject(it)
                onCommandReceived(json)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse message: $message", e)
                LoggingHelper.wWebSocket("Parse error: ${e.message}")
            }
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Log.i(TAG, "Remote control disconnected (code: $code, reason: $reason)")
        LoggingHelper.logConnectionEvent("CLOSE", conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.let {
            Log.e(TAG, "WebSocket error: ${it.message}", it)
            LoggingHelper.logWebSocketError("WebSocket error occurred", it)
        }
    }

    override fun onStart() {
        Log.i(TAG, "WebSocket server started on port $port")
        LoggingHelper.iWebSocket("Server started on port $port")
    }

    fun sendStatus(status: JSONObject) {
        val message = status.toString()
        broadcast(message)
    }
}