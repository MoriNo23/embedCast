package com.embedcast.tv

import android.util.Log
import android.webkit.WebView

/**
 * VideoPlayerManager - Encapsulates all video playback control logic
 * 
 * Provides methods for controlling JWPlayer and native video elements:
 * - Play/Pause toggle
 * - Seek operations (by seconds and percentage)
 * - Quality selection
 * - Subtitle toggle
 * - Player reset and resume
 */
class VideoPlayerManager(private val webView: WebView?) {

    companion object {
        private const val TAG = "VideoPlayerManager"
    }

    /**
     * Toggle play/pause state
     */
    fun playPause() {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    
                    if (hasJW && typeof jwplayer().getState === 'function') {
                        if (jwplayer().getState() === 'playing') {
                            jwplayer().pause();
                        } else {
                            jwplayer().play();
                        }
                    } else if (v) {
                        v.paused ? v.play() : v.pause();
                    } else {
                        var el = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);
                        if (el) el.click();
                    }
                } catch(e) {
                    console.log('Error in playPause: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    /**
     * Seek by delta seconds (positive = forward, negative = backward)
     */
    fun seek(deltaSeconds: Int) {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    
                    if (hasJW && typeof jwplayer().getPosition === 'function') {
                        var curr = jwplayer().getPosition();
                        jwplayer().seek(curr + $deltaSeconds);
                    } else if (v) {
                        v.currentTime += $deltaSeconds;
                    }
                } catch(e) {
                    console.log('Error in seek: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    /**
     * Seek to specific percentage of video duration
     * @param percent Value between 0.0 and 1.0
     */
    fun seekPercent(percent: Float) {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    
                    if (hasJW && typeof jwplayer().getDuration === 'function') {
                        var dur = jwplayer().getDuration();
                        if (dur > 0) {
                            jwplayer().seek(dur * $percent);
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("Saltando al " + Math.round($percent * 100) + "%");
                            }
                        }
                    } else if (v) {
                        v.currentTime = v.duration * $percent;
                    }
                } catch(e) {
                    console.log('Error in seekPercent: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    /**
     * Set video quality level (1-9 maps to quality index)
     */
    fun setQuality(level: Int) {
        val targetIdx = level - 1
        val script = """
            (function() {
                try {
                    if (typeof window.jwplayer !== 'undefined' && typeof jwplayer().getQualityLevels === 'function') {
                        var levels = jwplayer().getQualityLevels();
                        if (levels && $targetIdx >= 0 && $targetIdx < levels.length) {
                            jwplayer().setCurrentQuality($targetIdx);
                            setTimeout(function() {
                                try {
                                    var currentLevel = jwplayer().getCurrentQuality();
                                    var currentLabel = levels[currentLevel] ? levels[currentLevel].label : 'Unknown';
                                    if(typeof AndroidTV !== 'undefined') {
                                        AndroidTV.onQualityUpdated("Calidad: " + currentLabel);
                                    }
                                } catch(e) {
                                    console.log('Error getting current quality: ' + e);
                                    if(typeof AndroidTV !== 'undefined') {
                                        AndroidTV.onQualityUpdated("Calidad: Desconocida");
                                    }
                                }
                            }, 300);
                        } else {
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("Calidad $level no disponible");
                            }
                        }
                    } else {
                        if(typeof AndroidTV !== 'undefined') {
                            AndroidTV.showToast("Cambio de calidad no soportado en este reproductor");
                        }
                    }
                } catch(e) {
                    console.log('Error in setQuality: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    /**
     * Toggle subtitles on/off
     */
    fun toggleSubtitles() {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    
                    if (hasJW && typeof jwplayer().getCaptionsList === 'function') {
                        var captions = jwplayer().getCaptionsList();
                        if (captions && captions.length > 0) {
                            var currIdx = jwplayer().getCurrentCaptions();
                            var targetIdx = (currIdx > 0) ? 0 : 1;
                            jwplayer().setCurrentCaptions(targetIdx);
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast((targetIdx > 0) ? "Subtítulos Activados" : "Subtítulos Desactivados");
                            }
                        } else {
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("No hay subtítulos disponibles");
                            }
                        }
                    } else if (v && v.textTracks) {
                        var tracks = v.textTracks;
                        var anyActive = false;
                        for (var i = 0; i < tracks.length; i++) {
                            if (tracks[i].mode === 'showing') {
                                anyActive = true;
                                tracks[i].mode = 'hidden';
                            }
                        }
                        if (!anyActive && tracks.length > 0) {
                            tracks[0].mode = 'showing';
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("Subtítulos Activados");
                            }
                        } else if (anyActive) {
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("Subtítulos Desactivados");
                            }
                        } else {
                            if(typeof AndroidTV !== 'undefined') {
                                AndroidTV.showToast("No hay subtítulos disponibles");
                            }
                        }
                    } else {
                        if(typeof AndroidTV !== 'undefined') {
                            AndroidTV.showToast("Subtítulos no soportados en este reproductor");
                        }
                    }
                } catch(e) {
                    console.log('Error in toggleSubtitles: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }

    /**
     * Get current playback position
     */
    fun getCurrentPosition(callback: (Float) -> Unit) {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    var pos = 0;
                    
                    if (hasJW && typeof jwplayer().getPosition === 'function') {
                        pos = jwplayer().getPosition();
                    } else if (v) {
                        pos = v.currentTime;
                    }
                    return pos;
                } catch(e) {
                    androidLog('Error getting position: ' + e);
                    return 0;
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script) { value ->
            try {
                val position = value?.toFloatOrNull() ?: 0f
                callback(position)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse position value: ${e.message}")
                callback(0f)
            }
        }
    }

    /**
     * Get video duration
     */
    fun getDuration(callback: (Float) -> Unit) {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    var dur = 0;
                    
                    if (hasJW && typeof jwplayer().getDuration === 'function') {
                        dur = jwplayer().getDuration();
                    } else if (v) {
                        dur = v.duration || 0;
                    }
                    return dur;
                } catch(e) {
                    return 0;
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script) { value ->
            try {
                val duration = value?.toFloatOrNull() ?: 0f
                callback(duration)
            } catch (e: Exception) {
                callback(0f)
            }
        }
    }

    /**
     * Check if video is currently playing
     */
    fun isPlaying(callback: (Boolean) -> Unit) {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    var playing = false;
                    
                    if (hasJW && typeof jwplayer().getState === 'function') {
                        playing = jwplayer().getState() === 'playing';
                    } else if (v) {
                        playing = !v.paused && !v.ended;
                    }
                    return playing;
                } catch(e) {
                    return false;
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script) { value ->
            try {
                val playing = value?.toBoolean() ?: false
                callback(playing)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    /**
     * Load a video URL
     */
    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    /**
     * Force reload the current video (clears cache)
     */
    fun forceReload() {
        webView?.let { wv ->
            wv.clearCache(true)
            val currentUrl = wv.url
            if (!currentUrl.isNullOrEmpty()) {
                wv.loadUrl(currentUrl)
            }
        }
    }

    /**
     * Stop and reset the player
     */
    fun stop() {
        val script = """
            (function() {
                try {
                    var hasJW = typeof window.jwplayer !== 'undefined';
                    var v = document.querySelector('video');
                    
                    if (hasJW && typeof jwplayer().stop === 'function') {
                        jwplayer().stop();
                    } else if (v) {
                        v.pause();
                        v.currentTime = 0;
                    }
                } catch(e) {
                    console.log('Error in stop: ' + e);
                }
            })();
        """.trimIndent()
        webView?.evaluateJavascript(script, null)
    }
}