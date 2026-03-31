# Add project specific ProGuard rules here.

# Keep WebSocket classes
-keep class org.java_websocket.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes
-keepclassmembers class com.embedcast.remote.** { *; }
