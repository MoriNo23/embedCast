package com.embedcast.tv

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.os.Message

class VideoChromeClient : WebChromeClient() {
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        // Strict anti-popup: block all new window requests
        return false
    }
}