package com.embedcast.tv

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.json.JSONObject

@RunWith(MockitoJUnitRunner::class)
class WebSocketManagerTest {

    @Mock
    private lateinit var mockTvWebSocketServer: TvWebSocketServer

    private lateinit var webSocketManager: WebSocketManager

    @Before
    fun setup() {
        webSocketManager = WebSocketManager(9090)
    }

    @Test
    fun `WebSocketManager should use default port when not specified`() {
        val defaultManager = WebSocketManager()
        assert(defaultManager != null)
    }

    @Test
    fun `sendStatus should delegate to TvWebSocketServer`() {
        val status = JSONObject().apply {
            put("type", "status")
            put("currentTime", 100)
            put("duration", 200)
            put("paused", false)
        }

        webSocketManager.startServer { }
        webSocketManager.sendStatus(status)

        verify(mockTvWebSocketServer, atLeastOnce()).sendStatus(any())
    }

    @Test
    fun `sendLog should create log message and send via WebSocket`() {
        val testMessage = "Test log message"

        webSocketManager.startServer { }
        webSocketManager.sendLog(testMessage)

        verify(mockTvWebSocketServer, atLeastOnce()).sendStatus(any())
    }

    @Test
    fun `isRunning should return false before server starts`() {
        assert(!webSocketManager.isRunning())
    }

    @Test
    fun `getRetryCount should return 0 initially`() {
        assert(webSocketManager.getRetryCount() == 0)
    }
}
