package com.embedcast.tv

import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class PreferencesManagerTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setup() {
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putFloat(any(), any())).thenReturn(mockEditor)
        `when`(mockEditor.remove(any())).thenReturn(mockEditor)
        preferencesManager = PreferencesManager(mockSharedPreferences)
    }

    @Test
    fun `saveProgress should sanitize URL and save to SharedPreferences`() {
        val url = "https://example.com/video?id=123&token=abc"
        val timeMs = 120.5f

        preferencesManager.saveProgress(url, timeMs)

        val expectedKey = "progress_https___example_com_video_id_123_token_abc"
        verify(mockEditor).putFloat(eq(expectedKey), eq(timeMs))
        verify(mockEditor).apply()
    }

    @Test
    fun `loadProgress should sanitize URL and load from SharedPreferences`() {
        val url = "https://example.com/video?id=123"
        val expectedKey = "progress_https___example_com_video_id_123"
        val expectedValue = 45.0f

        `when`(mockSharedPreferences.getFloat(eq(expectedKey), eq(0f))).thenReturn(expectedValue)

        val result = preferencesManager.loadProgress(url)

        assert(result == expectedValue)
        verify(mockSharedPreferences).getFloat(eq(expectedKey), eq(0f))
    }

    @Test
    fun `loadProgress should return 0 for unknown URL`() {
        val url = "https://unknown.com/video"
        val expectedKey = "progress_https___unknown_com_video"

        `when`(mockSharedPreferences.getFloat(eq(expectedKey), eq(0f))).thenReturn(0f)

        val result = preferencesManager.loadProgress(url)

        assert(result == 0f)
    }

    @Test
    fun `clearProgress should remove progress for specific URL`() {
        val url = "https://example.com/video"
        val expectedKey = "progress_https___example_com_video"

        preferencesManager.clearProgress(url)

        verify(mockEditor).remove(eq(expectedKey))
        verify(mockEditor).apply()
    }

    @Test
    fun `clearAllProgress should remove all progress entries`() {
        val allKeys = mapOf(
            "progress_url1" to 10f,
            "progress_url2" to 20f,
            "other_key" to "value"
        )
        `when`(mockSharedPreferences.all).thenReturn(allKeys)

        preferencesManager.clearAllProgress()

        verify(mockEditor).remove("progress_url1")
        verify(mockEditor).remove("progress_url2")
        verify(mockEditor, never()).remove("other_key")
        verify(mockEditor).apply()
    }

    @Test
    fun `sanitizeUrl should replace non-alphanumeric characters with underscores`() {
        val url = "https://example.com/video?id=123&token=abc#fragment"
        val expected = "https___example_com_video_id_123_token_abc_fragment"

        val result = preferencesManager.loadProgress(url)

        verify(mockSharedPreferences).getFloat(eq(expected), eq(0f))
    }
}
