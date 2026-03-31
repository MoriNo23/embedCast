# EmbedCast Project - AI Assistant Guide

> **Purpose:** This document helps AI assistants understand the EmbedCast project structure, rules, and constraints. Read this before making any changes.

## 1. Project Overview

**EmbedCast** is an Android TV application for video casting and playback. It allows users to:
- Cast videos from a Linux host to an Android TV device
- Control playback remotely via WebSocket
- Manage video quality and subtitles
- Resume playback from saved positions

**Core Technology Stack:**
- Android TV (Leanback, Kotlin)
- WebSocket for remote control
- JWPlayer for video playback
- Linux CLI/Web GUI for host control

## 2. Architecture

```
┌─────────────────┐     WebSocket (8080)     ┌─────────────────┐
│                 │◄────────────────────────►│                 │
│   Linux Host    │                          │   Android TV    │
│   (embedCast-   │                          │   (embedCast-   │
│    host)        │                          │    tv)          │
└─────────────────┘                          └─────────────────┘
```

**Communication Protocol:**
- **Port:** 8080 (WebSocket)
- **Actions:** load, play, pause, stop, seek, quality, reload
- **Format:** JSON messages

## 3. Project Structure

```
embedCast Project/
├── .sisyphus/                          # Project management & plans
├── docs/                               # Documentation & session logs
├── embedCast-tv/                       # Android TV application
│   ├── app/
│   │   ├── build.gradle.kts            # Build configuration
│   │   └── src/main/
│   │       ├── java/com/tvremote/control/
│   │       │   ├── MainActivity.kt     # Main activity (454 lines)
│   │       │   ├── SplashActivity.kt   # Animated splash screen
│   │       │   ├── VideoPlayerManager.kt # Video controls
│   │       │   ├── VideoLoadManager.kt # Video loading
│   │       │   ├── WebSocketManager.kt # WebSocket server
│   │       │   ├── PreferencesManager.kt # SharedPreferences
│   │       │   ├── LoggingHelper.kt    # Centralized logging
│   │       │   └── ...                 # Other activities
│   │       ├── res/                    # Resources (layouts, icons)
│   │       └── AndroidManifest.xml
│   ├── local.properties                # SDK path configuration
│   └── gradle/                         # Gradle wrapper
├── embedCast-host/                     # Linux host controller
│   ├── cli-tool/                       # Command-line interface
│   └── web-gui/                        # Web-based GUI
├── embedCast-sdk/                      # Local Android SDK
└── experimental/                       # Work-in-progress features
    └── embedCast-updater/              # Update notification system
        ├── server.py                   # Update server
        └── deploy_update.sh            # Deployment script
```

## 4. Development Rules

### ✅ DO:
- Keep `MainActivity.kt` under 500 lines
- Use Kotlin for all new Android code
- Follow Android TV design guidelines (Leanback)
- Test WebSocket compatibility after changes
- Update documentation when changing protocols
- Use `PackageInfoCompat` for version checks
- Use `overrideActivityTransition()` for API 34+

### ❌ DON'T:
- Don't modify `embedCast-sdk/` (local SDK installation)
- Don't change WebSocket port (8080) without updating both sides
- Don't add new dependencies without approval
- Don't break backward compatibility with existing remote controls
- Don't use deprecated Android APIs
- Don't create files larger than 500 lines

### Naming Conventions:
- **Activities:** `*Activity.kt` (e.g., `SplashActivity.kt`)
- **Managers:** `*Manager.kt` (e.g., `VideoPlayerManager.kt`)
- **Helpers:** `*Helper.kt` (e.g., `LoggingHelper.kt`)
- **Layouts:** `activity_*.xml` (e.g., `activity_splash.xml`)

## 5. Build & Test Commands

```bash
# Navigate to TV app
cd embedCast-tv

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug

# Check for deprecation warnings
./gradlew compileDebugKotlin
```

## 6. Key Files Reference

| File                  | Purpose                      | Criticality  |
| --------------------- | ---------------------------- | ------------ |
| `MainActivity.kt`       | Main activity, video control | 🔴 Critical  |
| `WebSocketManager.kt`   | WebSocket server management  | 🔴 Critical  |
| `VideoPlayerManager.kt` | Video playback controls      | 🟡 Important |
| `PreferencesManager.kt` | Save/load video positions    | 🟡 Important |
| `AndroidManifest.xml`   | App configuration            | 🔴 Critical  |
| `build.gradle.kts`      | Build configuration          | 🔴 Critical  |

## 7. Dependencies

| Library                | Version | Purpose               |
| ---------------------- | ------- | --------------------- |
| `androidx.appcompat`     | 1.6.1   | Android compatibility |
| `androidx.core:core-ktx` | 1.12.0  | Kotlin extensions     |
| `androidx.media`         | 1.7.0   | Media session support |
| `Java-WebSocket`         | 1.5.4   | WebSocket server      |
| `junit`                  | 4.13.2  | Testing framework     |
| `mockito`                | 5.5.0   | Mocking framework     |
| `robolectric`            | 4.10.3  | Android unit tests    |

## 8. Known Issues & TODOs

### Current Issues:
- `dev-updater` is incomplete (update notification system)
- WebSocket protocol needs versioning
- No automated tests for WebSocket communication

### Planned Features:
- Adaptive icons for Android 8.0+
- CI/CD pipeline
- Multi-device casting support
- Video playlist management

## 9. Emergency Procedures

### If Build Fails:
1. Check `local.properties` SDK path
2. Run `./gradlew clean`
3. Clear Gradle caches: `rm -rf ~/.gradle/caches/`
4. Verify Android SDK is installed

### If WebSocket Stops Working:
1. Check port 8080 availability
2. Verify network connectivity
3. Restart `WebSocketManager`
4. Check `AndroidManifest.xml` for INTERNET permission

### If App Crashes:
1. Check logs: `adb logcat | grep EmbedCast`
2. Verify `minSdk` compatibility
3. Test on different Android TV devices
