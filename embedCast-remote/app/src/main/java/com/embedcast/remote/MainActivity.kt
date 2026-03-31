package com.embedcast.remote

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.embedcast.remote.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var webSocketClient: WebSocketClient? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val logMessages = mutableListOf<String>()
    private val maxLogLines = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateConnectionStatus(false)
    }

    private fun setupClickListeners() {
        binding.buttonConnect.setOnClickListener {
            if (webSocketClient != null) {
                disconnect()
            } else {
                connect()
            }
        }

        binding.buttonLoad.setOnClickListener {
            val url = binding.editTextURL.text.toString()
            if (url.isNotBlank()) {
                sendCommand("""{"action": "load", "url": "$url"}""")
            }
        }

        binding.buttonPlay.setOnClickListener {
            sendCommand("""{"action": "play"}""")
        }

        binding.buttonPause.setOnClickListener {
            sendCommand("""{"action": "pause"}""")
        }

        binding.buttonStop.setOnClickListener {
            sendCommand("""{"action": "stop"}""")
        }
    }

    private fun connect() {
        val ip = binding.editTextIP.text.toString().trim()
        if (ip.isBlank()) {
            addLog("Please enter TV IP address")
            return
        }

        val wsUri = if (ip.contains("://")) {
            URI(ip)
        } else {
            URI("ws://$ip:8080")
        }

        addLog("Connecting to $ip...")

        webSocketClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                runOnUiThread {
                    updateConnectionStatus(true)
                    addLog("Connected to $ip")
                    binding.buttonConnect.text = getString(R.string.disconnect)
                }
            }

            override fun onMessage(message: String?) {
                message?.let {
                    runOnUiThread {
                        addLog("TV: $it")
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                runOnUiThread {
                    updateConnectionStatus(false)
                    addLog("Disconnected: $reason")
                    binding.buttonConnect.text = getString(R.string.connect)
                    webSocketClient = null
                }
            }

            override fun onError(ex: Exception?) {
                runOnUiThread {
                    addLog("Error: ${ex?.message}")
                    updateConnectionStatus(false)
                }
            }
        }

        webSocketClient?.connect()
    }

    private fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        updateConnectionStatus(false)
        binding.buttonConnect.text = getString(R.string.connect)
        addLog("Disconnected")
    }

    private fun sendCommand(command: String) {
        if (webSocketClient == null) {
            addLog("Not connected to TV")
            return
        }

        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send(command)
            addLog("Sent: $command")
        } else {
            addLog("Connection lost")
        }
    }

    private fun updateConnectionStatus(connected: Boolean) {
        val color = if (connected) {
            ContextCompat.getColor(this, R.color.embedcast_primary)
        } else {
            ContextCompat.getColor(this, R.color.embedcast_error)
        }

        val drawable = binding.viewStatus.background as? GradientDrawable
            ?: GradientDrawable().also { binding.viewStatus.background = it }

        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(color)

        binding.textViewStatus.text = if (connected) {
            getString(R.string.connected)
        } else {
            getString(R.string.disconnected)
        }

        binding.buttonLoad.isEnabled = connected
        binding.buttonPlay.isEnabled = connected
        binding.buttonPause.isEnabled = connected
        binding.buttonStop.isEnabled = connected
    }

    private fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"

        logMessages.add(logEntry)
        if (logMessages.size > maxLogLines) {
            logMessages.removeAt(0)
        }

        binding.textViewLog.text = logMessages.joinToString("\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        disconnect()
    }
}
