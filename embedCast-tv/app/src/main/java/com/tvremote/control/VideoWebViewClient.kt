package com.embedcast.tv

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse

open class VideoWebViewClient(private val listener: ErrorListener) : WebViewClient() {
    
    interface ErrorListener {
        fun onError(description: String)
        fun onRetry(url: String)
        fun onConnectionInterrupted()
        fun getResumeTime(): Float
    }

    private var hasRetried = false
    private var lastUrl: String? = null

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        if (request?.isForMainFrame == true) {
            val url = request.url.toString()
            handleError(view, url, error?.description?.toString() ?: "Unknown error")
        }
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        if (request?.isForMainFrame == true) {
            val url = request.url.toString()
            handleError(view, url, "HTTP Error: ${errorResponse?.statusCode}")
        }
    }

    private fun handleError(view: WebView?, url: String, description: String) {
        if (!hasRetried) {
            hasRetried = true
            lastUrl = url
            listener.onRetry(url)
            view?.loadUrl(url)
        } else {
            listener.onError(description)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        
        if (url == lastUrl) {
            hasRetried = false
        }

        val script = """
            (function() {
                window.open = function() { return null; };
                document.querySelectorAll('a[target="_blank"]').forEach(el => el.removeAttribute('target'));

                const resumeTime = AndroidTV.getResumeTime();
                let hasResumed = false;

                const monitor = () => {
                    const video = document.querySelector('video');
                    var hasJW = typeof window.jwplayer !== 'undefined' && typeof window.jwplayer().seek === 'function';
                    
                    if (!navigator.onLine) {
                        AndroidTV.onConnectionInterrupted();
                        return;
                    }

                    // 1. Resume logic (Robust for JWPlayer and custom embeds)
                    if (resumeTime > 1 && !hasResumed) {
                        if (hasJW && jwplayer().getState() !== 'idle' && jwplayer().getState() !== 'buffering') {
                            if (jwplayer().getPosition() < 1) {
                                jwplayer().seek(resumeTime);
                                hasResumed = true;
                                console.log('EmbedCast: Resumed with JWPlayer at ' + resumeTime);
                            }
                        } else if (video && video.readyState >= 1) {
                            if (video.currentTime < 1) {
                                video.currentTime = resumeTime;
                                hasResumed = true;
                                console.log('EmbedCast: Resumed generic video at ' + resumeTime);
                            }
                        }
                    }

                    // 2. State update
                    if (hasJW && typeof jwplayer().getPosition === 'function') {
                        var curr = jwplayer().getPosition() || 0;
                        var dur = jwplayer().getDuration() || 0;
                        var state = jwplayer().getState();
                        var paused = (state === 'paused' || state === 'idle');
                        AndroidTV.onVideoUpdate(curr, dur, paused);
                    } else if (video) {
                        AndroidTV.onVideoUpdate(
                            parseFloat(video.currentTime) || 0,
                            parseFloat(video.duration) || 0,
                            video.paused
                        );
                    }

                    // 3. Health check listeners
                    if (video) {
                        if (!video.hasMovieOnListeners) {
                            video.addEventListener('error', () => AndroidTV.onConnectionInterrupted());
                            video.addEventListener('stalled', () => AndroidTV.onConnectionInterrupted());
                            video.hasMovieOnListeners = true;
                        }
                        if (video.networkState === 3 || video.error) {
                            AndroidTV.onConnectionInterrupted();
                            return;
                        }
                    }

                    // 4. Quality detection (always runs, independent of video element)
                    const qualityLabels = Array.from(document.querySelectorAll('.quality, .settings-menu, [aria-label*="quality"]'))
                        .map(el => el.innerText)
                        .filter(t => t && t.length < 10)
                        .join(', ');
                    
                    if (qualityLabels) AndroidTV.onQualitiesFound(qualityLabels);
                    }
                };

                if (window.movieOnTimer) clearInterval(window.movieOnTimer);
                window.movieOnTimer = setInterval(monitor, 2000);
            })();
        """.trimIndent()
        
        view?.evaluateJavascript(script, null)
    }
}