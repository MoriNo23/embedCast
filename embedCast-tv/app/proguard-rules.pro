# ProGuard rules for EmbedCast TV App
# These rules prevent important classes from being obfuscated/removed

# Keep WebSocket classes - CRITICAL for release builds
-keep class org.java_websocket.** { *; }
-keepclassmembers class org.java_websocket.** { *; }
-keepclassmembers class org.java_websocket.server.WebSocketServer { *; }
-keepclassmembers class org.java_websocket.WebSocket { *; }
-keepclassmembers class org.java_websocket.handshake.ClientHandshake { *; }

# Keep JSON parsing classes
-keep class org.json.** { *; }
-keepclassmembers class org.json.** { *; }

# Keep Android TV Leanback classes
-keep class androidx.leanback.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep application class
-keep public class com.embedcast.tv.** { *; }
-keepclassmembers public class com.embedcast.tv.** { *; }

# Keep all classes that extend WebSocketServer
-keep class * extends org.java_websocket.server.WebSocketServer { *; }

# Keep all classes that implement WebSocket
-keep class * implements org.java_websocket.WebSocket { *; }

# Keep exception classes
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.RuntimeException

# Keep network classes
-keep class java.net.** { *; }
-keepclassmembers class java.net.** { *; }

# Keep InetSocketAddress and related classes
-keep class java.net.InetSocketAddress { *; }
-keep class java.net.InetAddress { *; }

# Don't warn about WebSocket library
-dontwarn org.java_websocket.**
